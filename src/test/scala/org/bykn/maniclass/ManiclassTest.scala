/*
Copyright 2012 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.bykn.maniclass

import org.specs._

class ManiclassTest extends Specification {
  noDetailedDiffs() //Fixes issue for scala 2.9
  "Maniclass" should {
    "Wrap and unwrap" in {
      val mc = Maniclass()
      val g0 = mc.wrap(List(1,2,3,4))
      mc.unwrap[List[Int]](g0).get must be_==(List(1,2,3,4))
      g0.isInstanceOf[Gettable] must beTrue
      val g1 = mc.wrap(List(2,3))
      g1.getClass.getName must be_==(g0.getClass.getName)
    }
    "Handle subtyping" in {
      val mc = Maniclass()
      val g0 = mc.wrap(List(1,2,3,4))
      mc.unwrap[List[Any]](g0).isDefined must beTrue
    }
    "Handle reinitialization" in {
      val mc = Maniclass()
      val l = List(1,2,3,4)
      val g0 = mc.wrap(l)
      val mc2 = Maniclass(mc.toManifestSeq)
      mc2.unwrap[List[Int]](g0).get must be_==(List(1,2,3,4))
      (g0.get eq l) must beTrue
    }
  }
}
