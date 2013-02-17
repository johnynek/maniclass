package org.bykn.maniclass

import scala.collection.mutable.{Map => MMap, Buffer}

object Maniclass {
  def apply(): Maniclass = new Maniclass(Seq.empty)
  def apply(mf: Seq[Manifest[_]]): Maniclass = new Maniclass(mf)
  def apply(mf: Seq[Manifest[_]], cl: ClassLoader): Maniclass = new Maniclass(mf) {
    override def parentClassLoader = cl
  }
}

/**
 * Many java-based systems use the Class[_] to do run-time typing.
 * With scala, List[T] all have the same type. This code allocates
 * a separate wrapper class indexed for each distinct wrapper Manifest[T].
 * This allows, for instance, giving a List[Int] and a List[String] different
 * wrapper classes. This enables better interop with systems like Hadoop, Kryo,
 * and Storm, all of which inherit the switch-on-getClass semantics.
 */
class Maniclass(inits: Seq[Manifest[_]]) {

  def parentClassLoader: ClassLoader = this.getClass.getClassLoader

  // The class loader that creates the Gettable instances
  val classLoader: ClassLoader = new ClassLoader(parentClassLoader) {
    override def findClass(name: String) = {
      Generator.indexFromName(name) match {
        case None => throw new java.lang.ClassNotFoundException(name)
        case Some(idx) =>
        val bytes = Generator.classBytes(idx)
        defineClass(bytes, 0, bytes.length)
      }
    }
  }
  // Make sure classes have been loaded up to a certain index
  def gettableClass(eidx: Int): Class[_<:Gettable] = {
    val name = Generator.nameFromIndex(eidx)
    classLoader.loadClass(name).asInstanceOf[Class[_<:Gettable]]
  }
  /** Wrap an instance of a type known at compile-type with a gettable
  */
  def wrap[T](t: T)(implicit mf: Manifest[T]): Gettable =
    gettableFn(mf).apply(t.asInstanceOf[AnyRef])

  /** unwraps an instance of Gettable and checks that it contains a subtype of T
   */
  def unwrap[T](g: Gettable)(implicit mf: Manifest[T]): Option[T] = {
    // Make sure the wrapped item is a subclass of T
    manifestOf(g.getClass)
      .filter { _ <:< mf }
      .map { cls =>
        g.get.asInstanceOf[T]
      }
  }

  /** Given a class, get the Manifest if an instance was previously wrapped
  */
  def manifestOf(t: Class[_]): Option[Manifest[_]] = class2mf.get(t.getName)

  /** Returns the manifests in the order they were created.
   * can be used to instantiate the same Manifest <-> Gettable mappings
   */
  def toManifestSeq: Seq[Manifest[_]] =
    mfmap.toSeq.sortBy { _._2._1 }.map { _._1 }

  /**
   * Private/protected implementation details below
   */
  private val mfmap = MMap[Manifest[_], (Int, AnyRef => Gettable)]()
  private val class2mf = MMap[String, Manifest[_]]()
  private var idx = -1

  inits.foreach { mf => gettableFn(mf) }

  private def nextIdx: Int = {
    idx = idx + 1
    idx
  }

  private def cons(cls: Class[_]): AnyRef => Gettable = { x: AnyRef =>
    cls.getConstructors.apply(0).newInstance(x).asInstanceOf[Gettable]
  }

  private def gettableFn[T](mf: Manifest[T]): AnyRef => Gettable = {
    mfmap.getOrElseUpdate(mf, {
      val idx = nextIdx
      val newClass = gettableClass(idx)
      class2mf += newClass.getName -> mf
      (idx -> cons(newClass))
    })._2
  }

}

