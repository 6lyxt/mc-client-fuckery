package at.dietze.esddump;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class Dump {
    public static void main(String[] args) throws Throwable {
        JarInputStream jis = new JarInputStream(new FileInputStream("./EsdeathClient.jar"));
        JarOutputStream jos = new JarOutputStream(new FileOutputStream("./Out.jar"));

        byte[] buffer = new byte[8192];
        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null){
            int size;
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((size = jis.read(buffer)) != -1){
                bos.write(buffer, 0, size);
            }
            bos.close();

            jos.putNextEntry(new JarEntry(je.getName()));
            if(je.getName().endsWith(".class")){
                ClassReader cr = new ClassReader(bos.toByteArray());
                ClassWriter cw = new ClassWriter(Opcodes.ASM5);
               try {
                   cr.accept(new RemappingClassAdapter(new ClassVisitor(Opcodes.ASM5, cw) {
                   }, new Remapper() {
                       @Override
                       public String map(String typeName) {
                           if (typeName.equals(ConcurrentHashMap.class.getName().replace(".", "/"))) {
                               System.out.println(bos);
                           }
                           System.out.println(typeName);
                           return super.map(typeName);
                       }
                   }) {

                   }, ClassReader.EXPAND_FRAMES);
                   jos.write(cw.toByteArray());
               } catch(ArrayIndexOutOfBoundsException out){
                   System.out.println("out of index");
               }
            } else {
                jos.write(bos.toByteArray());
            }
        }
        jos.close();
        jis.close();
        System.out.println("finished");
    }
}
