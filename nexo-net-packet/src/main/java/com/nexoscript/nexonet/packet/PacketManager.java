package com.nexoscript.nexonet.packet;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PacketManager {
    private static final Map<String, Class<? extends Packet>> packetRegistry = new HashMap<>();

    public static void registerPacketType(String type, Class<? extends Packet> clazz) {
        packetRegistry.put(type, clazz);
    }

    public static JSONObject toJson(Packet packet) {
        JSONObject json = new JSONObject();
        json.put("type", packet.getType());
        Field[] fields = packet.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                json.put(field.getName(), field.get(packet));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Packet fromJson(JSONObject json) {
        try {
            String type = json.getString("type");
            Class<? extends Packet> clazz = packetRegistry.get(type);
            if (clazz == null) {
                throw new IllegalArgumentException("Unknown packet type: " + type);
            }
            Packet packet = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (json.has(field.getName())) {
                    Object value = json.get(field.getName());
                    field.set(packet, value);
                }
            }
            return packet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
