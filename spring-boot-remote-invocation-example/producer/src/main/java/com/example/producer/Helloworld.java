package com.example.producer;
import jdk.internal.org.objectweb.asm.*;
import org.remote.invocation.starter.network.point.service.PotintProxyService;
import org.remote.invocation.starter.network.point.service.impl.PotintProxyServiceImpl;
import java.lang.reflect.InvocationTargetException;

/**
 * @author liucheng
 * @create 2018-06-14 16:55
 **/
public class Helloworld extends ClassLoader implements Opcodes {
    public static void main(final String args[]) {
        String name = "org.remote.invocation.starter.network.point.service.impl.ExampleServiceImpl";
        String className = name.replace('.', '/');
        Class interfaceClasss = PotintProxyService.class;
        Class interfaceClasssImpl = PotintProxyServiceImpl.class;
        //定义一个叫做Example的类
        ClassWriter cw = new ClassWriter(0);
        String[] var6 = {Type.getInternalName(interfaceClasss)};
        cw.visit(V1_1, ACC_PUBLIC, className, null, Type.getInternalName(interfaceClasssImpl), var6);

        //生成默认的构造方法
        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        //生成构造方法的字节码指令
        mw.visitVarInsn(ALOAD, 0);
        mw.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(interfaceClasssImpl), "<init>", "()V");
        mw.visitInsn(RETURN);
        mw.visitMaxs(1, 1);
        mw.visitEnd();
        // 获取生成的class文件对应的二进制流
        byte[] code = cw.toByteArray();
        //将二进制流写到本地磁盘上
//        FileOutputStream fos = new FileOutputStream("Example.class");
//        fos.write(code);
//        fos.close();
        //直接将二进制流加载到内存中
        Helloworld loader = new Helloworld();
        Class<?> exampleClass = loader.defineClass(name, code, 0, code.length);
        Object obj = null;
        try {
            obj = exampleClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //通过反射调用main方法
        try {
            exampleClass.getMethods()[0].invoke(obj, "aaaaaaaa");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }
}
