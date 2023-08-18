package game.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMainTraceAgent {

    /**
     * 热更新入口，需要在MANIFEST.MF指定classloader类名
     */
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("agent main called!");
        System.out.println("args:" + agentArgs);
        Class<?>[] allClass = inst.getAllLoadedClasses();
        for(Class<?> c:allClass){
            if(!c.getSimpleName().equals("Test")){
                continue;
            }
            String path = "E:\\slggame\\server\\httpserver\\target\\classes\\game\\loader\\Test.class";
            File file = new File(path);
            try{
                byte[] bytes = fileToBytes(file);
                System.out.println("文件大小："+bytes.length);
                ClassDefinition classDefinition = new ClassDefinition(c,bytes);
                inst.redefineClasses(classDefinition);
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.println("热更完成");
        }
//        inst.addTransformer(new Transformer(agentArgs), true);
//        //class加载后执行，如果要加载前执行，要执行redefineClasses
//        inst.retransformClasses(Test.class);
    }

    public static byte[] fileToBytes(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        return bytes;
    }

    //    private static class Transformer implements ClassFileTransformer {
    //        private final String targetClassName;
    //
    //        public Transformer(String targetClassName) {
    //            this.targetClassName = targetClassName;
    //        }
    //
    //        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    //            className = className.replaceAll("/", ".");
    //            if (!className.equals(targetClassName)) {
    //                return null;
    //            }
    //            System.out.println("transform: " + className);
    //
    //            ClassPool classPool = ClassPool.getDefault();
    //            classPool.appendClassPath(new LoaderClassPath(loader)); // 将要修改的类的classpath加入到ClassPool中，否则找不到该类
    //            try {
    //                CtClass ctClass = classPool.get(className);
    //                for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
    //                    if (Modifier.isPublic(ctMethod.getModifiers()) && !ctMethod.getName().equals("main")) {
    //                        // 修改字节码
    //                        ctMethod.addLocalVariable("begin", CtClass.longType);
    //                        ctMethod.addLocalVariable("end", CtClass.longType);
    //                        ctMethod.insertBefore("begin = System.currentTimeMillis();");
    //                        ctMethod.insertAfter("end = System.currentTimeMillis();");
    //                        ctMethod.insertAfter("System.out.println(\"方法" + ctMethod.getName() + "耗时\"+ (end - begin) +\"ms\");");
    //                    }
    //                }
    //                ctClass.detach();
    //                return ctClass.toBytecode();
    //            } catch (Exception e) {
    //                e.printStackTrace();
    //            }
    //            return classfileBuffer;
    //        }
    //    }
    //}
}
