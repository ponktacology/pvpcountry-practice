package country.pvp.practice.data.mongo;

import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import country.pvp.practice.data.DataObject;
import country.pvp.practice.data.Repository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MongoRepository<V extends DataObject> implements Repository<V> {

    protected final MongoDatabase database;

    @Override
    public void save(V entity) {
        database.getCollection(entity.getCollection()).replaceOne(Filters.eq("_id", entity.getId()), entity.getDocument(), new ReplaceOptions().upsert(true));
    }

    @Override
    public void load(V entity) {
        org.bson.Document document = database.getCollection(entity.getCollection()).find(Filters.eq("_id", entity.getId())).first();
        if (document == null) return;
        entity.applyDocument(document);
    }

    @Override
    public void delete(V entity) {
        database.getCollection(entity.getCollection()).deleteOne(Filters.eq("_id", entity.getId()));
    }
}