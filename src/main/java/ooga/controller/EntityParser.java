package ooga.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class is responsible for parsing the entity based on the corresponding sim file
 * @author Nick Ward
 */
public class EntityParser {
    private String entityName;
    private String entityType;
    private String XPosition;
    private String YPosition;
    private Properties entityProperties;
    private Map<String, String> attributeMap;
    private static final String ENTITY_DIRECTORY = "data/entity/%s.sim";

    /**
     * Constructor for EntityParser
     * @param entityName
     * @param entityData
     */
    public EntityParser(String entityName, String[] entityData) {
        this.entityName = entityName;
        this.entityType = entityData[0];
        this.XPosition = entityData[1];
        this.YPosition = entityData[2];
        GeneralParser simParser = new GeneralParser();
        entityProperties = simParser.getSimData(String.format(ENTITY_DIRECTORY, entityType));
        attributeMap = new HashMap<>();
        createAttributeMap();
    }

    /**
     * Returns the attribute map
     */
    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    /**
     * Creates the attribute and state map
     */
    private void createAttributeMap() {
        attributeMap.put("XPosition", XPosition);
        attributeMap.put("YPosition", YPosition);
        attributeMap.put("EntityType", entityType);
        attributeMap.put("Name", entityName);

        entityProperties.entrySet().forEach(entry->{
            String key = (String) entry.getKey();
            String value = ((String) entry.getValue()).replaceAll("\\s+","");
            attributeMap.put(key, value);
        });
    }
}
