package me.akaishin.obfuscator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Obfuscator {
    private static final Map<String, byte[]> f = new HashMap<>();
    private static final List<ClassNode> c = new ArrayList<>();
    private static boolean string = false;

    public static void main(String[] args) {
        run("C:\\Users\\akais\\Desktop\\CrackedObfuscator-FreeVersion\\Cracked-2.2-release.jar");
    }

    public static void run(String path) {
        File file = new File(path);

        if (!file.exists()) {
            System.err.println("File not found");
            return;
        }

        System.out.println("Start: " + file.getName());

        try (JarFile jarFile = new JarFile(file)) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry jarEntry = entries.nextElement();
                try (InputStream inputStream = jarFile.getInputStream(jarEntry)) {
                    final byte[] bytes = IOUtils.toByteArray(inputStream);
                    if (!jarEntry.getName().endsWith(".class")) {
                        f.put(jarEntry.getName(), bytes);
                        continue;
                    }
                    try {
                        if (String.format("%X%X%X%X", bytes[0], bytes[1], bytes[2], bytes[3]).equals("CAFEBABE")) {
                            final ClassNode classNode = new ClassNode();
                            new ClassReader(bytes).accept(classNode, ClassReader.EXPAND_FRAMES);
                            c.add(classNode);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (ClassNode classNode : c) {
            if (classNode.name.startsWith("net/futureclient/client/mixin")) continue;
            
            if (string) {
                MethodNode WN;
                if (!((classNode.access & Opcodes.ACC_INTERFACE) != 0) && !((classNode.access & Opcodes.ACC_ABSTRACT) != 0) && !((classNode.access & Opcodes.ACC_ANNOTATION) != 0) && !((classNode.access & Opcodes.ACC_ENUM) != 0)) {
                    String methodName = get(new Random().nextInt(20));
                    WN = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, methodName, "(Ljava/lang/String;)Ljava/lang/String;", null, null);
                    for (MethodNode methodNode : classNode.methods) {
    
                        if (methodNode.name.startsWith("lambda$")) continue;
    
                        for (AbstractInsnNode abstractInsnNode : methodNode.instructions.toArray()) {
                            if (abstractInsnNode.getType() == AbstractInsnNode.LDC_INSN) {
                                LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                                if (ldcInsnNode.cst instanceof String) {
                                    String s = (String)ldcInsnNode.cst;
                                    InsnList il = new InsnList();
                                    il.add(new LdcInsnNode(Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8))));
                                    il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, methodName, "(Ljava/lang/String;)Ljava/lang/String;", false));
                                    methodNode.instructions.insert(ldcInsnNode, il);
                                    methodNode.instructions.remove(ldcInsnNode);
                                }
                            }
                        }
                    }
                    WN.visitCode();
                    WN.visitTypeInsn(Opcodes.NEW, "java/lang/String");
                    WN.visitInsn(Opcodes.DUP);
                    WN.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Base64", "getDecoder", "()Ljava/util/Base64$Decoder;", false);
                    WN.visitVarInsn(Opcodes.ALOAD, 0);
                    WN.visitFieldInsn(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
                    WN.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "getBytes", "(Ljava/nio/charset/Charset;)[B", false);
                    WN.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Base64$Decoder", "decode", "([B)[B", false);
                    WN.visitFieldInsn(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;");
                    WN.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V", false);
                    WN.visitInsn(Opcodes.ARETURN);
                    WN.visitMaxs(5, 2);
                    WN.visitEnd();
                    classNode.methods.add(WN);
                }
                WN = new MethodNode(Opcodes.ACC_PROTECTED, "____HELLO_WORLD___", "()Ljava/lang/String;", null, null);
                WN.visitCode();
                WN.visitLdcInsn("https://github.com/XG2025-Akaishin");
                WN.visitVarInsn(Opcodes.ASTORE, 0);
                WN.visitLdcInsn("Obfuscated -> CrackedObfuscator");
                WN.visitInsn(Opcodes.ARETURN);
                WN.visitEnd();
                classNode.methods.add(WN);
            }
        }

        File file_o = new File(path.substring(0, path.length() - ".jar".length()) + "-Obfuscated.jar");

        if (file_o.exists() && !file_o.delete()) {
            System.err.println("Could not delete out file");
            return;
        }

        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(file_o))) {
            for (ClassNode classNode : c) {
                jarOutputStream.putNextEntry(new JarEntry(classNode.name + ".class"));
                jarOutputStream.write(toByteArray(classNode));
                jarOutputStream.closeEntry();
            }

            for (Map.Entry<String, byte[]> entry : f.entrySet()) {
                jarOutputStream.putNextEntry(new JarEntry(entry.getKey()));
                jarOutputStream.write(entry.getValue());
                jarOutputStream.closeEntry();
            }
            System.out.println("End: " + file_o.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(int r) {
        StringBuilder sb = new StringBuilder();
        sb.append("_");
        for (int j = 0; j < r; j++) {
            if (new Random().nextBoolean()) {
                sb.append("i");
            } else {
                sb.append("I");
            }
        }
        return sb.toString();
        
    }

    public static byte[] toByteArray(ClassNode classNode) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        try {
            classNode.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            writer = new ClassWriter(0);
            classNode.accept(writer);
            return writer.toByteArray();
        }
    }
}
