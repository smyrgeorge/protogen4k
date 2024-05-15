# protogen4k

![Build](https://github.com/smyrgeorge/protogen4k/actions/workflows/ci.yml/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.github.smyrgeorge/protogen4k)
![GitHub License](https://img.shields.io/github/license/smyrgeorge/protogen4k)
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/smyrgeorge/protogen4k)
![GitHub issues](https://img.shields.io/github/issues/smyrgeorge/protogen4k)

A small and simple `.proto` file generator. Generates a protobuf schema from the given kotlin classes.

### Usage

```xml

<dependency>
    <groupId>io.github.smyrgeorge</groupId>
    <artifactId>protogen4k</artifactId>
    <version>x.y.z</version>
</dependency>
```

or using gradle:

```kotlin
implementation("io.github.smyrgeorge:protogen4k:x.y.z")
```

### Examples

Let's see an example.

```kotlin
package io.github.smyrgeorge.datasuite.converter.protobuf.examples

import io.github.smyrgeorge.datasuite.converter.protobuf.KotlinClassToProtobuf
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile

class Main

@ProtoFile(name = "a_class.proto", schema = "schema1")
data class AClass(
    val a: String,
    val b: List<AnEnum>
) {
    enum class AnEnum {
        A, B
    }
}

fun main(args: Array<String>) {
    KotlinClassToProtobuf.generate(
        topicPrefix = "sample",
        workDirectory = "examples/src/main/proto",
        classes = listOf(AClass::class),
        protoPackage = "io.github.smyrgeorge.datasuite.proto"
    )
}
```

The above `kotlin` code will generate 2 `.proto` files.

```protobuf
// file: a_class.proto
syntax = "proto3";

package io.github.smyrgeorge.datasuite.proto;

option java_package = "io.github.smyrgeorge.datasuite.proto";
option java_outer_classname = "AClassProto";

import "default.proto";

message AClass {
  string a = 1;
  repeated AClassAnEnum.Enum b = 2;
}
```

```protobuf
// file: default.proto
syntax = "proto3";

package io.github.smyrgeorge.datasuite.proto;

option java_package = "io.github.smyrgeorge.datasuite.proto";
option java_outer_classname = "DefaultProto";

message AClassAnEnum {
  enum Enum {
    PROTO_EMPTY = 0;
    A = 1;
    B = 2;
  }
}
```
