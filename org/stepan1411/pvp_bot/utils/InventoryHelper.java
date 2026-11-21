package org.stepan1411.pvp_bot.utils;

import java.lang.reflect.Field;
import net.minecraft.class_1661;

public class InventoryHelper {
    private static Field selectedSlotField;
    
    static {
        try {
            selectedSlotField = class_1661.class.getDeclaredField("selectedSlot");
            selectedSlotField.setAccessible(true);
        } catch (NoSuchFieldException e) {

            try {
                selectedSlotField = class_1661.class.getDeclaredField("field_7545");
                selectedSlotField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException("Failed to find selectedSlot field", ex);
            }
        }
    }
    
    public static void setSelectedSlot(class_1661 inventory, int slot) {
        try {
            selectedSlotField.setInt(inventory, slot);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set selectedSlot", e);
        }
    }
    
    public static int getSelectedSlot(class_1661 inventory) {
        try {
            return selectedSlotField.getInt(inventory);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get selectedSlot", e);
        }
    }
}
