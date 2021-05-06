package fr.flowsqy.stelytab.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public class TeamPacketSender {

    private static final Constructor<?> packetPlayOutScoreboardTeamConstructor;

    private static final Field idField;
    private static final Field displayNameField;
    private static final Field prefixField;
    private static final Field suffixField;
    private static final Field nameTagVisibilityField;
    private static final Field collisionRuleField;
    private static final Field colorField;
    private static final Field playersField;
    private static final Field methodField;
    private static final Field optionField;

    private static final Constructor<?> chatComponentTextConstructor;

    private static final java.lang.reflect.Method getHandlePlayerMethod;
    private static final Field playerConnectionField;
    private static final java.lang.reflect.Method sendPacketMethod;

    private static final Object BLACK;
    private static final Object DARK_BLUE;
    private static final Object DARK_GREEN;
    private static final Object DARK_AQUA;
    private static final Object DARK_RED;
    private static final Object DARK_PURPLE;
    private static final Object GOLD;
    private static final Object GRAY;
    private static final Object DARK_GRAY;
    private static final Object BLUE;
    private static final Object GREEN;
    private static final Object AQUA;
    private static final Object RED;
    private static final Object LIGHT_PURPLE;
    private static final Object YELLOW;
    private static final Object WHITE;
    private static final Object OBFUSCATED;
    private static final Object BOLD;
    private static final Object STRIKETHROUGH;
    private static final Object UNDERLINE;
    private static final Object ITALIC;
    private static final Object RESET;

    /*
     * Initialization of Reflection
     */
    static {
        try {
            final String versionName = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            final String nms = "net.minecraft.server." + versionName + ".";
            final Class<?> packetPlayOutScoreboardTeamClass = Class.forName(nms + "PacketPlayOutScoreboardTeam");

            packetPlayOutScoreboardTeamConstructor = packetPlayOutScoreboardTeamClass.getDeclaredConstructor();

            idField = packetPlayOutScoreboardTeamClass.getDeclaredField("a");
            displayNameField = packetPlayOutScoreboardTeamClass.getDeclaredField("b");
            prefixField = packetPlayOutScoreboardTeamClass.getDeclaredField("c");
            suffixField = packetPlayOutScoreboardTeamClass.getDeclaredField("d");
            nameTagVisibilityField = packetPlayOutScoreboardTeamClass.getDeclaredField("e");
            collisionRuleField = packetPlayOutScoreboardTeamClass.getDeclaredField("f");
            colorField = packetPlayOutScoreboardTeamClass.getDeclaredField("g");
            playersField = packetPlayOutScoreboardTeamClass.getDeclaredField("h");
            methodField = packetPlayOutScoreboardTeamClass.getDeclaredField("i");
            optionField = packetPlayOutScoreboardTeamClass.getDeclaredField("j");

            final Class<?> chatComponentTextClass = Class.forName(nms + "ChatComponentText");
            chatComponentTextConstructor = chatComponentTextClass.getDeclaredConstructor(String.class);

            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + versionName + ".entity.CraftPlayer");
            getHandlePlayerMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            final Class<?> entityPlayerClass = Class.forName(nms + "EntityPlayer");
            playerConnectionField = entityPlayerClass.getDeclaredField("playerConnection");
            final Class<?> playerConnectionClass = Class.forName(nms + "PlayerConnection");
            final Class<?> packetClass = Class.forName(nms + "Packet");
            sendPacketMethod = playerConnectionClass.getDeclaredMethod("sendPacket", packetClass);

            final Class<?> enumChatFormat = Class.forName(nms + "EnumChatFormat");

            BLACK = getColor("BLACK", enumChatFormat);
            DARK_BLUE = getColor("DARK_BLUE", enumChatFormat);
            DARK_GREEN = getColor("DARK_GREEN", enumChatFormat);
            DARK_AQUA = getColor("DARK_AQUA", enumChatFormat);
            DARK_RED = getColor("DARK_RED", enumChatFormat);
            DARK_PURPLE = getColor("DARK_PURPLE", enumChatFormat);
            GOLD = getColor("GOLD", enumChatFormat);
            GRAY = getColor("GRAY", enumChatFormat);
            DARK_GRAY = getColor("DARK_GRAY", enumChatFormat);
            BLUE = getColor("BLUE", enumChatFormat);
            GREEN = getColor("GREEN", enumChatFormat);
            AQUA = getColor("AQUA", enumChatFormat);
            RED = getColor("RED", enumChatFormat);
            LIGHT_PURPLE = getColor("LIGHT_PURPLE", enumChatFormat);
            YELLOW = getColor("YELLOW", enumChatFormat);
            WHITE = getColor("WHITE", enumChatFormat);
            OBFUSCATED = getColor("OBFUSCATED", enumChatFormat);
            BOLD = getColor("BOLD", enumChatFormat);
            STRIKETHROUGH = getColor("STRIKETHROUGH", enumChatFormat);
            UNDERLINE = getColor("UNDERLINE", enumChatFormat);
            ITALIC = getColor("ITALIC", enumChatFormat);
            RESET = getColor("RESET", enumChatFormat);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can not load PacketPlayOutScoreboardTeam fields", e);
        }

        packetPlayOutScoreboardTeamConstructor.setAccessible(true);

        idField.setAccessible(true);
        displayNameField.setAccessible(true);
        prefixField.setAccessible(true);
        suffixField.setAccessible(true);
        nameTagVisibilityField.setAccessible(true);
        collisionRuleField.setAccessible(true);
        colorField.setAccessible(true);
        playersField.setAccessible(true);
        methodField.setAccessible(true);
        optionField.setAccessible(true);

        chatComponentTextConstructor.setAccessible(true);

        getHandlePlayerMethod.setAccessible(true);
        playerConnectionField.setAccessible(true);
        sendPacketMethod.setAccessible(true);
    }

    /**
     * Gets nms color Object from EnumChatColor
     *
     * @param color              The name of the color field
     * @param enumChatColorClass The EnumChatColor class
     * @return Nms color Object
     * @throws ReflectiveOperationException If there is no field with this name in the EnumChatColor class
     */
    private static Object getColor(String color, Class<?> enumChatColorClass) throws ReflectiveOperationException {
        final Field colorField = enumChatColorClass.getDeclaredField(color);
        colorField.setAccessible(true);
        return colorField.get(null);
    }

    /**
     * Gets Nms color Object for given ChatColor enum constant
     *
     * @param color The ChatColor to convert
     * @return Nms color Object corresponding to the given ChatColor
     */
    private static Object getColor(ChatColor color) {
        switch (color) {
            case BLACK:
                return BLACK;
            case DARK_BLUE:
                return DARK_BLUE;
            case DARK_GREEN:
                return DARK_GREEN;
            case DARK_AQUA:
                return DARK_AQUA;
            case DARK_RED:
                return DARK_RED;
            case DARK_PURPLE:
                return DARK_PURPLE;
            case GOLD:
                return GOLD;
            case GRAY:
                return GRAY;
            case DARK_GRAY:
                return DARK_GRAY;
            case BLUE:
                return BLUE;
            case GREEN:
                return GREEN;
            case AQUA:
                return AQUA;
            case RED:
                return RED;
            case LIGHT_PURPLE:
                return LIGHT_PURPLE;
            case YELLOW:
                return YELLOW;
            case WHITE:
                return WHITE;
            case MAGIC:
                return OBFUSCATED;
            case BOLD:
                return BOLD;
            case STRIKETHROUGH:
                return STRIKETHROUGH;
            case UNDERLINE:
                return UNDERLINE;
            case ITALIC:
                return ITALIC;
            case RESET:
                return RESET;
        }

        return null;
    }

    /**
     * Get the remove packet
     *
     * @param id The id of the team to remove
     * @return The remove packet
     */
    public static Object getRemove(String id) {
        try {
            final Object packet = packetPlayOutScoreboardTeamConstructor.newInstance();
            idField.set(packet, id);
            methodField.set(packet, Method.REMOVE.getMethod());
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the create packet
     *
     * @param name    the Name data of the created team
     * @param players The players inside the team
     * @return The create packet
     */
    public static Object getCreate(Name name, List<String> players) {
        try {
            final Object packet = packetPlayOutScoreboardTeamConstructor.newInstance();
            idField.set(packet, name.getId());
            prefixField.set(packet, chatComponentTextConstructor.newInstance(name.getPrefix()));
            suffixField.set(packet, chatComponentTextConstructor.newInstance(name.getSuffix()));
            colorField.set(packet, getColor(name.getColor()));
            playersField.set(packet, players);
            methodField.set(packet, Method.CREATE.getMethod());
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method for clientside interpretation of the information
     */
    public enum Method {

        //https://wiki.vg/Protocol#Teams
        /*
         *  0 Create
         *  1 Remove
         *  2 Update
         *  3 Add player
         *  4 Remove Player
         */

        CREATE(0),
        REMOVE(1),
        UPDATE_INFO(2),
        ADD_PLAYERS(3),
        REMOVE_PLAYERS(4);

        private final int method;

        Method(int method) {
            this.method = method;
        }

        public int getMethod() {
            return method;
        }

        @Override
        public String toString() {
            return "Method{" +
                    "method=" + method +
                    "} " + super.toString();
        }
    }

}
