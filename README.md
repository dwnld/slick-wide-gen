# slick-gen-wide


## Summary

This repo contains a utility that allows generate SLICK bindings for schema's with wide tables(22 columns or more)

## Notes

There are two subprojects in this repo. One is intended to be consumed as a library while the other is the code that tests that library. Since sbt requires all plugins/project code to be run on scala 2.10, the library project is cross compiled in 2.10. The test project only uses scala 2.11 since, wide case classes are only available from then on.

## TODO

autoIncLastAsOption currently doesn't work. There's some work to get it to work. I'm leaving it for a later date
