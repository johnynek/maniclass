maniclass
=========

Generate unique classes for each unique scala type.

An example goes a long ways:
```scala
scala> import org.bykn.maniclass.Maniclass
import org.bykn.maniclass.Maniclass

scala> val mc = Maniclass()
mc: org.bykn.maniclass.Maniclass = org.bykn.maniclass.Maniclass@4658ef71

scala> mc.wrap(List(1,2,3,4))
res0: org.bykn.maniclass.Gettable = org.bykn.maniclass.Gettable0@12dbd81

scala> res0.get
res1: java.lang.Object = List(1, 2, 3, 4)

scala> mc.unwrap[List[Int]](res0)
res2: Option[List[Int]] = Some(List(1, 2, 3, 4))

scala> mc.unwrap[List[Any]](res0)
res3: Option[List[Any]] = Some(List(1, 2, 3, 4))

scala> mc.unwrap[List[String]](res0)
res4: Option[List[String]] = None
```

The motivation here is to deal with Hadoop serialization which switches on Class. This allows us to
wrap each object with an instance of `org.bykn.maniclass.Gettable` whose getName is sufficient to
lookup the original Manifest. This should give more efficient type-safe serialization of scala
objects.
