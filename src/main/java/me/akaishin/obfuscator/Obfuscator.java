package me.akaishin.obfuscator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
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
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class Obfuscator {
    private static final Map<String, byte[]> f = new HashMap<>();
    private static final List<ClassNode> c = new ArrayList<>();
    private static final String ch = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom r = new SecureRandom();
    public static int counter = 0;

    public static List<Data> Datas;

    // Obfuscar las cadenas de textos true(si lo obfuscara) false(no)
    private static boolean string = true;

    // mixin en el cual se ignorara de momento
    private static String mixin = "net.futureclient.client.mixin"; // ruta del mixin

    /**
     * Ignorar lista de clases por el nombre example:
     */
    public static List<String> ignore_Class_List = new ArrayList<>(
        Arrays.asList(
            "net.futureclient.loader.CrackedFuture"
            )
        );

    /**
     * Ignorar metodos de clases en especifico
     */
    public static List<String> ignore_Method_List = new ArrayList<>(
        Arrays.asList(
            "net.futureclient.loader.CrackedFuture$onInitialize",
                "net.futureclient.loader.CrackedFuture$onInitializeClient"
            )
        );

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
            counter = 0;
            if (!mixin.isEmpty()) {
                if (classNode.name.startsWith(mixin.replace(".", "/"))) continue;
            }
            if (check_Ignore_Class(classNode)) continue;
            if (string) {
                MethodNode WN;
                if (!((classNode.access & Opcodes.ACC_INTERFACE) != 0) && !((classNode.access & Opcodes.ACC_ABSTRACT) != 0) && !((classNode.access & Opcodes.ACC_ANNOTATION) != 0) && !((classNode.access & Opcodes.ACC_ENUM) != 0) && !((classNode.access & Opcodes.ACC_RECORD) != 0)) {
                    String fieldName = get(new Random().nextInt(20));
                    String nms = "decrypt";
                    classNode.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, fieldName, "[Ljava/lang/String;", null, null);
                    Datas = new ArrayList<>(); 
                    String methodName = get(new Random().nextInt(20));
                    for (MethodNode methodNode : classNode.methods) {
                        if (check_Ignore_ClassMethod(classNode, methodNode)) continue;

                        if (methodNode.name.startsWith("lambda$")) continue;
    
                        for (AbstractInsnNode abstractInsnNode : methodNode.instructions.toArray()) {
                            if (abstractInsnNode.getType() == AbstractInsnNode.LDC_INSN) {
                                LdcInsnNode ldcInsnNode = (LdcInsnNode) abstractInsnNode;
                                if (ldcInsnNode.cst instanceof String) {
                                    String s = (String)ldcInsnNode.cst;
                                    InsnList il = new InsnList();
                                    
                                    il.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldName, "[Ljava/lang/String;"));
                                    il.add(new LdcInsnNode(counter));
                                    il.add(new InsnNode(Opcodes.AALOAD));

                                    String k = GEN(1 + new Random().nextInt(47));

                                    Datas.add(new Data( encrypt(s, k), k, counter));

                                    counter++;
                                    methodNode.instructions.insert(ldcInsnNode, il);
                                    methodNode.instructions.remove(ldcInsnNode);
                                }
                            }
                        }
                    }

                    WN = get(classNode, "<clinit>");
                    if (WN == null) {
                        WN = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                        classNode.methods.add(WN);
                    }
        
                    InsnList instructions = new InsnList();
                    instructions.add(new IntInsnNode(Opcodes.BIPUSH, Datas.size()));
                    instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));
                    instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, fieldName, "[Ljava/lang/String;"));
            
                    for (Data sb27Data : Datas) {
                        instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, fieldName, "[Ljava/lang/String;"));
                        instructions.add(new InsnNode(Opcodes.DUP));
                        instructions.add(new LdcInsnNode(sb27Data.getNumber()));
                        instructions.add(new LdcInsnNode(sb27Data.getData_String()));
                        instructions.add(new LdcInsnNode(sb27Data.getKey_String()));
                        instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name, methodName, "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", false));
                        instructions.add(new InsnNode(Opcodes.AASTORE));
                    }

                    for (MethodNode methodNode : classNode.methods) {
                        if (methodNode.name.equals("<clinit>")) {
                            if (methodNode.instructions != null && methodNode.instructions.getFirst() != null) {
                                methodNode.instructions.insert(methodNode.instructions.getFirst(), instructions);
                            } else {
                                methodNode.instructions.add(instructions);
                            }
                            break;
                        }
                    }

                    try {
                        MethodNode m = get(t(), nms);
                        m.name = methodName;
                        classNode.methods.add(m);   
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }

                if (((classNode.access & Opcodes.ACC_RECORD) != 0)) continue;
                WN = new MethodNode(Opcodes.ACC_PROTECTED, "PROTECTED____HELLO_WORLD___", "()Ljava/lang/String;", null, null);
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

    public static String GEN(int c) {
        StringBuilder result = new StringBuilder(c);
        for (int i = 0; i < c; i++) {
            int d = r.nextInt(ch.length());
            result.append(ch.charAt(d));
        }
        return result.toString();
    }

    public static ClassNode t() throws IOException {
        final ClassReader cr = new ClassReader(Obfuscator.class.getResourceAsStream("/" + Obfuscator.class.getName().replace('.', '/') + ".class"));
        final ClassNode cn = new ClassNode();

        cr.accept(cn, 0);

        return cn;
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
    

    public static boolean check_Ignore_ClassMethod(ClassNode classNode, MethodNode methodNode) {
        for (String name : ignore_Method_List) {
            String[] list_dat = name.split("$"); 
            if (classNode.name.equals(list_dat[0].replace(".", "/")) && methodNode.name.equals(list_dat[1])) {
                return true;
            }
        } 
        return false;
    }

    public static boolean check_Ignore_Class(ClassNode classNode) {
        for (String name : ignore_Class_List) {
            if (classNode.name.equals(name.replace(".", "/"))) {
                return true;
            }
        }
        return false;
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

    public static MethodNode get(final ClassNode classNode, final String strin) {
        for (final MethodNode method : classNode.methods)
            if (method.name.equals(strin))
                return method;
        return null;
    }

    public static String decrypt(String obj, String key) {
        obj = new String(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] keyChars = key.toCharArray();
        int i = 0;
        for (char c : obj.toCharArray()) {
            sb.append((char) (c ^ keyChars[i % keyChars.length]));
            i++;
        }
        return sb.toString();
    }

    public static String encrypt(String obj, String key) {
        StringBuilder sb = new StringBuilder();
        char[] keyChars = key.toCharArray();
        int i = 0;
        for (char c : obj.toCharArray()) {
            sb.append((char) (c ^ keyChars[i % keyChars.length]));
            i++;
        }
        return new String(Base64.getEncoder().encode(sb.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static class Data {
        private String data_String;
        private String key_String;
        private int number;
    
        public Data(String data_String, String key_String, int number) {
            this.data_String = data_String;
            this.key_String = key_String;
            this.number = number;
        }
    
        public String getData_String() {
            return this.data_String;
        }
    
        public String getKey_String() {
            return this.key_String;
        }
    
        public int getNumber() {
            return this.number;
        }
    }
}
