Mill
====

```shell script
../mill/mill dataProvider.compile
../mill/mill webApp docker.build

./mill -i contrib.playlib.compile 
ci/publish-local.sh && cp ~/mill-release ./mill
./mill -i mill.scalalib.GenIdea/idea
```
Note that mill is published locally next to this project.

Issues
------
GenIdea support when using Scala 2.13
Play 2.8 support
sbt-release plugin alternative
