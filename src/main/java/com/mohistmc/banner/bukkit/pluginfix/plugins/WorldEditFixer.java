package com.mohistmc.banner.bukkit.pluginfix.plugins;

import com.mohistmc.banner.bukkit.pluginfix.IPluginFixer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

public class WorldEditFixer implements IPluginFixer {

    @Override
    public byte[] injectPluginFix(String className, byte[] clazz) {
        if (className.equals("com.sk89q.worldedit.bukkit.WorldEditPlugin")) {
            System.setProperty("worldedit.bukkit.adapter", "com.sk89q.worldedit.bukkit.adapter.impl.v1_20_R1.PaperweightAdapter");
        } else if (className.equals("com.sk89q.worldedit.bukkit.BukkitAdapter")) {
            return patchBukkitAdapter(clazz);
        } else if (className.equals("com.sk89q.worldedit.bukkit.adapter.impl.v1_20_R1.PaperweightAdapter")) {
            return patchPaperweightAdapter(clazz);
        }
        return clazz;
    }

    private byte[] patchBukkitAdapter(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals("adapt") && methodNode.desc.equals("(Lcom/sk89q/worldedit/world/block/BlockType;)Lorg/bukkit/Material;")) {
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/sk89q/worldedit/world/block/BlockType", "getId", "()Ljava/lang/String;"));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(WorldEditFixer.class), "adaptHook", "(Ljava/lang/String;)Lorg/bukkit/Material;"));
                insnList.add(new InsnNode(Opcodes.ARETURN));
                methodNode.instructions = insnList;
            }

            if (methodNode.name.equals("adapt") && methodNode.desc.equals("(Lcom/sk89q/worldedit/world/item/ItemType;)Lorg/bukkit/Material;")) {
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/sk89q/worldedit/world/item/ItemType", "getId", "()Ljava/lang/String;"));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(WorldEditFixer.class), "adaptHook", "(Ljava/lang/String;)Lorg/bukkit/Material;"));
                insnList.add(new InsnNode(Opcodes.ARETURN));
                methodNode.instructions = insnList;
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    private byte[] patchPaperweightAdapter(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        for1:
        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals("getProperties") && methodNode.desc.equals("(Lcom/sk89q/worldedit/world/block/BlockType;)Ljava/util/Map;")) {
                ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode insnNode = iterator.next();
                    if (insnNode instanceof InsnNode) {
                        if (insnNode.getOpcode() == Opcodes.ATHROW) {
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.POP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Collections", "emptyMap", "()Ljava/util/Map;"));
                            insnList.add(new InsnNode(Opcodes.ARETURN));
                            methodNode.instructions.insertBefore(insnNode, insnList);
                            methodNode.instructions.remove(insnNode);
                            break for1;
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
