package org.remote.invocation.starter.utils;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;

/**
 * asm字节码操作
 *
 * @author liucheng
 * @create 2018-06-14 18:47
 **/
public class ASMUtils extends ClassLoader {
    private static class LazyHolder {
        private static final ASMUtils INSTANCE = new ASMUtils();
    }

    private ASMUtils() {

    }

    public static final ASMUtils getInstance() {
        return ASMUtils.LazyHolder.INSTANCE;
    }


    /**
     * 创建class并且加载到内存
     *
     * @param name              要创建的class名称 相当于 class.getName()
     * @param interfaceClasss   要实现的的接口class，可以是null
     * @param extendsClasssImpl 要继承的类class
     * @return 返回创建好的class对象
     */
    public static Class createClass(String name, Set<Class> interfaceClasss, Class extendsClasssImpl) {
        String className = name.replace('.', '/');
        //定义一个叫做Example的类
        ClassWriter cw = new ClassWriter(0);
        if (extendsClasssImpl == null) {
            extendsClasssImpl = Object.class;
        }
        if (interfaceClasss == null || interfaceClasss.isEmpty()) {
            cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, className, null, Type.getInternalName(extendsClasssImpl), null);
        } else {
            Set<String> interfaceSet = new HashSet<>();
            for (Class las : interfaceClasss) {
                interfaceSet.add(Type.getInternalName(las));
            }
            cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, className, null, Type.getInternalName(extendsClasssImpl), interfaceSet.toArray(new String[interfaceSet.size()]));
        }
        //生成默认的构造方法
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        //生成构造方法的字节码指令
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(extendsClasssImpl), "<init>", "()V");
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(1, 1);
        mw.visitEnd();
        // 获取生成的class文件对应的二进制流
        byte[] code = cw.toByteArray();
        //直接将二进制流加载到内存中
        return ASMUtils.getInstance().defineClass(name, code, 0, code.length);
    }
}
