# This file is a part of OpenCV project.
# It is a subject to the license terms in the LICENSE file found in the top-level directory
# of this distribution and at http://opencv.org/license.html.
#
# Copyright (C) 2018, Intel Corporation, all rights reserved.
# Third party copyrights are property of their respective owners.
#
# Use this script to get the text graph representation (.pbtxt) of SSD-based
# deep learning network trained in TensorFlow Object Detection API.
# Then you can import it with a binary frozen graph (.pb) using readNetFromTensorflow() function.
# See details and examples on the following wiki page: https://github.com/opencv/opencv/wiki/TensorFlow-Object-Detection-API
import tensorflow as tf
import argparse
from math import sqrt
from tensorflow.core.framework.node_def_pb2 import NodeDef
from tensorflow.tools.graph_transforms import TransformGraph
from google.protobuf import text_format

parser = argparse.ArgumentParser(description='Run this script to get a text graph of '
                                             'SSD model from TensorFlow Object Detection API. '
                                             'Then pass it with .pb file to cv::dnn::readNetFromTensorflow function.')
parser.add_argument('--input', required=True, help='Path to frozen TensorFlow graph.')
parser.add_argument('--output', required=True, help='Path to output text graph.')
parser.add_argument('--num_classes', default=90, type=int, help='Number of trained classes.')
parser.add_argument('--min_scale', default=0.2, type=float, help='Hyper-parameter of ssd_anchor_generator from config file.')
parser.add_argument('--max_scale', default=0.95, type=float, help='Hyper-parameter of ssd_anchor_generator from config file.')
parser.add_argument('--num_layers', default=6, type=int, help='Hyper-parameter of ssd_anchor_generator from config file.')
parser.add_argument('--aspect_ratios', default=[1.0, 2.0, 0.5, 3.0, 0.333], type=float, nargs='+',
                    help='Hyper-parameter of ssd_anchor_generator from config file.')
parser.add_argument('--image_width', default=300, type=int, help='Training images width.')
parser.add_argument('--image_height', default=300, type=int, help='Training images height.')
args = parser.parse_args()

# Nodes that should be kept.
keepOps = ['Conv2D', 'BiasAdd', 'Add', 'Relu6', 'Placeholder', 'FusedBatchNorm',
           'DepthwiseConv2dNative', 'ConcatV2', 'Mul', 'MaxPool', 'AvgPool', 'Identity']

# Nodes attributes that could be removed because they are not used during import.
unusedAttrs = ['T', 'data_format', 'Tshape', 'N', 'Tidx', 'Tdim', 'use_cudnn_on_gpu',
               'Index', 'Tperm', 'is_training', 'Tpaddings']

# Node with which prefixes should be removed
prefixesToRemove = ('MultipleGridAnchorGenerator/', 'Postprocessor/', 'Preprocessor/')

# Read the graph.
with tf.gfile.FastGFile(args.input, 'rb') as f:
    graph_def = tf.GraphDef()
    graph_def.ParseFromString(f.read())

inpNames = ['image_tensor']
outNames = ['num_detections', 'detection_scores', 'detection_boxes', 'detection_classes']
graph_def = TransformGraph(graph_def, inpNames, outNames, ['sort_by_execution_order'])

def getUnconnectedNodes():
    unconnected = []
    for node in graph_def.node:
        unconnected.append(node.name)
        for inp in node.input:
            if inp in unconnected:
                unconnected.remove(inp)
    return unconnected

removedNodes = []

# Detect unfused batch normalization nodes and fuse them.
def fuse_batch_normalization():
    # Add_0 <-- moving_variance, add_y
    # Rsqrt <-- Add_0
    # Mul_0 <-- Rsqrt, gamma
    # Mul_1 <-- input, Mul_0
    # Mul_2 <-- moving_mean, Mul_0
    # Sub_0 <-- beta, Mul_2
    # Add_1 <-- Mul_1, Sub_0
    nodesMap = {node.name: node for node in graph_def.node}
    subgraph = ['Add',
        ['Mul', 'input', ['Mul', ['Rsqrt', ['Add', 'moving_variance', 'add_y']], 'gamma']],
        ['Sub', 'beta', ['Mul', 'moving_mean', 'Mul_0']]]
    def checkSubgraph(node, targetNode, inputs, fusedNodes):
        op = targetNode[0]
        if node.op == op and (len(node.input) >= len(targetNode) - 1):
            fusedNodes.append(node)
            for i, inpOp in enumerate(targetNode[1:]):
                if isinstance(inpOp, list):
                    if not node.input[i] in nodesMap or \
                       not checkSubgraph(nodesMap[node.input[i]], inpOp, inputs, fusedNodes):
                        return False
                else:
                    inputs[inpOp] = node.input[i]

            return True
        else:
            return False

    nodesToRemove = []
    for node in graph_def.node:
        inputs = {}
        fusedNodes = []
        if checkSubgraph(node, subgraph, inputs, fusedNodes):
            name = node.name
            node.Clear()
            node.name = name
            node.op = 'FusedBatchNorm'
            node.input.append(inputs['input'])
            node.input.append(inputs['gamma'])
            node.input.append(inputs['beta'])
            node.input.append(inputs['moving_mean'])
            node.input.append(inputs['moving_variance'])
            text_format.Merge('f: 0.001', node.attr["epsilon"])
            nodesToRemove += fusedNodes[1:]
    for node in nodesToRemove:
        graph_def.node.remove(node)

fuse_batch_normalization()

# Removes Identity nodes
def removeIdentity():
    identities = {}
    for node in graph_def.node:
        if node.op == 'Identity':
            identities[node.name] = node.input[0]
            graph_def.node.remove(node)

    for node in graph_def.node:
        for i in range(len(node.input)):
            if node.input[i] in identities:
                node.input[i] = identities[node.input[i]]

removeIdentity()

# Remove extra nodes and attributes.
for i in reversed(range(len(graph_def.node))):
    op = graph_def.node[i].op
    name = graph_def.node[i].name

    if (not op in keepOps) or name.startswith(prefixesToRemove):
        if op != 'Const':
            removedNodes.append(name)

        del graph_def.node[i]
    else:
        for attr in unusedAttrs:
            if attr in graph_def.node[i].attr:
                del graph_def.node[i].attr[attr]

# Remove references to removed nodes except Const nodes.
for node in graph_def.node:
    for i in reversed(range(len(node.input))):
        if node.input[i] in removedNodes:
            del node.input[i]

# Connect input node to the first layer
assert(graph_def.node[0].op == 'Placeholder')
# assert(graph_def.node[1].op == 'Conv2D')
weights = graph_def.node[1].input[0]
for i in range(len(graph_def.node[1].input)):
    graph_def.node[1].input.pop()
graph_def.node[1].input.append(graph_def.node[0].name)
graph_def.node[1].input.append(weights)

# Create SSD postprocessing head ###############################################

# Concatenate predictions of classes, predictions of bounding boxes and proposals.
def tensorMsg(values):
    if all([isinstance(v, float) for v in values]):
        dtype = 'DT_FLOAT'
        field = 'float_val'
    elif all([isinstance(v, int) for v in values]):
        dtype = 'DT_INT32'
        field = 'int_val'
    else:
        raise Exception('Wrong values types')

    msg = 'tensor { dtype: ' + dtype + ' tensor_shape { dim { size: %d } }' % len(values)
    for value in values:
        msg += '%s: %s ' % (field, str(value))
    return msg + '}'

def addConstNode(name, values):
    node = NodeDef()
    node.name = name
    node.op = 'Const'
    text_format.Merge(tensorMsg(values), node.attr["value"])
    graph_def.node.extend([node])

def addConcatNode(name, inputs, axisNodeName):
    concat = NodeDef()
    concat.name = name
    concat.op = 'ConcatV2'
    for inp in inputs:
        concat.input.append(inp)
    concat.input.append(axisNodeName)
    graph_def.node.extend([concat])

addConstNode('concat/axis_flatten', [-1])
addConstNode('PriorBox/concat/axis', [-2])

for label in ['ClassPredictor', 'BoxEncodingPredictor']:
    concatInputs = []
    for i in range(args.num_layers):
        # Flatten predictions
        flatten = NodeDef()
        inpName = 'BoxPredictor_%d/%s/BiasAdd' % (i, label)
        flatten.input.append(inpName)
        flatten.name = inpName + '/Flatten'
        flatten.op = 'Flatten'

        concatInputs.append(flatten.name)
        graph_def.node.extend([flatten])
    addConcatNode('%s/concat' % label, concatInputs, 'concat/axis_flatten')

# Add layers that generate anchors (bounding boxes proposals).
scales = [args.min_scale + (args.max_scale - args.min_scale) * i / (args.num_layers - 1)
          for i in range(args.num_layers)] + [1.0]

priorBoxes = []
addConstNode('reshape_prior_boxes_to_4d', [1, 2, -1, 1])
for i in range(args.num_layers):
    priorBox = NodeDef()
    priorBox.name = 'PriorBox_%d' % i
    priorBox.op = 'PriorBox'
    priorBox.input.append('BoxPredictor_%d/BoxEncodingPredictor/BiasAdd' % i)
    priorBox.input.append(graph_def.node[0].name)  # image_tensor

    text_format.Merge('b: false', priorBox.attr["flip"])
    text_format.Merge('b: false', priorBox.attr["clip"])

    if i == 0:
        widths = [0.1, args.min_scale * sqrt(2.0), args.min_scale * sqrt(0.5)]
        heights = [0.1, args.min_scale / sqrt(2.0), args.min_scale / sqrt(0.5)]
    else:
        widths = [scales[i] * sqrt(ar) for ar in args.aspect_ratios]
        heights = [scales[i] / sqrt(ar) for ar in args.aspect_ratios]

        widths += [sqrt(scales[i] * scales[i + 1])]
        heights += [sqrt(scales[i] * scales[i + 1])]
    widths = [w * args.image_width for w in widths]
    heights = [h * args.image_height for h in heights]
    text_format.Merge(tensorMsg(widths), priorBox.attr["width"])
    text_format.Merge(tensorMsg(heights), priorBox.attr["height"])
    text_format.Merge(tensorMsg([0.1, 0.1, 0.2, 0.2]), priorBox.attr["variance"])

    graph_def.node.extend([priorBox])

    # Reshape from 1x2xN to 1x2xNx1
    reshape = NodeDef()
    reshape.name = priorBox.name + '/4d'
    reshape.op = 'Reshape'
    reshape.input.append(priorBox.name)
    reshape.input.append('reshape_prior_boxes_to_4d')
    graph_def.node.extend([reshape])

    priorBoxes.append(reshape.name)

addConcatNode('PriorBox/concat', priorBoxes, 'PriorBox/concat/axis')

# Sigmoid for classes predictions and DetectionOutput layer
sigmoid = NodeDef()
sigmoid.name = 'ClassPredictor/concat/sigmoid'
sigmoid.op = 'Sigmoid'
sigmoid.input.append('ClassPredictor/concat')
graph_def.node.extend([sigmoid])

detectionOut = NodeDef()
detectionOut.name = 'detection_out'
detectionOut.op = 'DetectionOutput'

detectionOut.input.append('BoxEncodingPredictor/concat')
detectionOut.input.append(sigmoid.name)
detectionOut.input.append('PriorBox/concat')

text_format.Merge('i: %d' % (args.num_classes + 1), detectionOut.attr['num_classes'])
text_format.Merge('b: true', detectionOut.attr['share_location'])
text_format.Merge('i: 0', detectionOut.attr['background_label_id'])
text_format.Merge('f: 0.6', detectionOut.attr['nms_threshold'])
text_format.Merge('i: 100', detectionOut.attr['top_k'])
text_format.Merge('s: "CENTER_SIZE"', detectionOut.attr['code_type'])
text_format.Merge('i: 100', detectionOut.attr['keep_top_k'])
text_format.Merge('f: 0.01', detectionOut.attr['confidence_threshold'])
text_format.Merge('b: true', detectionOut.attr['loc_pred_transposed'])

graph_def.node.extend([detectionOut])

while True:
    unconnectedNodes = getUnconnectedNodes()
    unconnectedNodes.remove(detectionOut.name)
    if not unconnectedNodes:
        break

    for name in unconnectedNodes:
        for i in range(len(graph_def.node)):
            if graph_def.node[i].name == name:
                del graph_def.node[i]
                break

# Save as text.
tf.train.write_graph(graph_def, "", args.output, as_text=True)
