/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

/**
 * A collection of general utility methods used in several locations.
 * 
 * @author Haitham
 */
public class StringUtils {

    /**
     * Gets a string equivalent to the provided one but without white spaces.
     * @param text the text from which white spaces are to be removed
     * @return the white spaces free text
     */
    public static String getSpacesFreeText(String text) {
        String spacesFreeText = "";
        for (int i = 0; i < text.length(); i++) {
            if(!Character.isWhitespace(text.charAt(i))) {
                spacesFreeText += text.charAt(i);
            }
        }
        return spacesFreeText;
    }
}
