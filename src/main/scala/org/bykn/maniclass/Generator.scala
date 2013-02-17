package org.bykn.maniclass

import org.objectweb.asm._

import Opcodes._

// Generates new classes on demand indexed by an integer
object Generator {
  val packageName = "org/bykn/maniclass/"
  val gennedPackage = packageName + "gen/"
  val gettable = "Gettable"
  val prefix = packageName + gettable
  val gettableIface = Array[String](prefix)

  def classBytes(idx: Int): Array[Byte] = {
    val thisClass = nameFromIndex(idx)
    val cw = new ClassWriter(0);
    cw.visit(V1_7, ACC_PUBLIC + ACC_SUPER, thisClass, null,
      "java/lang/Object", gettableIface);
    //Create the field
    var fv = cw.visitField(ACC_PRIVATE, "value", "Ljava/lang/Object;", null, null);
    fv.visitEnd();
    //Constructor
    var mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitFieldInsn(PUTFIELD, thisClass, "value", "Ljava/lang/Object;");
    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 2);
    mv.visitEnd();
    //Get method
    mv = cw.visitMethod(ACC_PUBLIC, "get", "()Ljava/lang/Object;", null, null);
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(GETFIELD, thisClass, "value", "Ljava/lang/Object;");
    mv.visitInsn(ARETURN);
    mv.visitMaxs(1, 1);
    mv.visitEnd();
    cw.visitEnd();
    cw.toByteArray();
  }

  def nameFromIndex(idx: Int): String = prefix + idx.toString
  def indexFromName(name: String): Option[Int] = {
    if(name.substring(0, prefix.length) == prefix) {
      Some(name.substring(prefix.length, name.size).toInt)
    }
    else {
      None
    }
  }
}
