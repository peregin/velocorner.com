# API Gateway
  * single entry point to the stack
  * expose API from schema, see app.apibuilder.io
  * convert and proxy to internal requests (e.g. Thrift or TypeScript)
  * authentication (e.g. JWT)
  * mapping to the internal service topology, use service discovery for calling services
  In addition:
  * caching
  * retry policies, circuit breaking
  * throttling
  * IP white/black-listing