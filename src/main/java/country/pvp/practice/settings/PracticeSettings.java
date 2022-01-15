package country.pvp.practice.settings;

import country.pvp.practice.util.data.DataObject;
import country.pvp.practice.util.serialization.LocationAdapter;
import lombok.Data;
import org.bson.Document;
import org.bukkit.Location;

@Data
public class PracticeSettings implements DataObject {

    private Location spawnLocation;
    private Location editorLocation;

    @Override
    public String getCollection() {
        return "settings";
    }

    @Override
    public String getId() {
        return "practice-settings";
    }

    @Override
    public Document getDocument() {
        Document document = new Document("_id", getId());

        document.put("spawnLocation", LocationAdapter.toJson(spawnLocation));
        document.put("editorLocation", LocationAdapter.toJson(editorLocation));

        return document;
    }

    @Override
    public void applyDocument(Document document) {
        spawnLocation = LocationAdapter.fromJson(document.getString("spawnLocation"));
        editorLocation = LocationAdapter.fromJson(document.getString("editorLocation"));
    }
}
