Mill
====

```shell script
# run it with the newly compiled mill (with play 2.8 support)
../mill/out/assembly/dest/mill dataProvider.compile
../mill/out/assembly/dest/mill webApp.compile
../mill/out/assembly/dest/mill webApp docker.build

./mill -i contrib.playlib.compile
ci/publish-local.sh && cp ~/mill-release ./mill

../mill/out/assembly/dest/mill -i mill.scalalib.GenIdea/idea
../mill/out/assembly/dest/mill -i mill.bsp.BSP/install
```
Note that mill is published locally next to this project.

Issues
------
Play 2.8 support
sbt-release plugin alternative
