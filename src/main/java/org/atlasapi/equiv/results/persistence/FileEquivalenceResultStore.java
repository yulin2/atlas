package org.atlasapi.equiv.results.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.atlasapi.equiv.results.EquivalenceResult;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

public class FileEquivalenceResultStore implements EquivalenceResultStore {

    private StoredEquivalenceResultTranslator translator = new StoredEquivalenceResultTranslator();
    private final File directory;
    
    public FileEquivalenceResultStore(File directory) {
        if(!directory.isDirectory()) {
            throw new IllegalArgumentException("Must be a directory");
        }
        if(!directory.exists()) {
            throw new IllegalArgumentException("Directory does not exist");
        }
        this.directory = directory;
    }
    
    @Override
    public <T extends Content> StoredEquivalenceResult store(
            EquivalenceResult<T> result) {
        StoredEquivalenceResult storedEquivalenceResult = translator.toStoredEquivalenceResult(result);
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileFromCanonicalUri(result.subject().getCanonicalUri())));
            os.writeObject(storedEquivalenceResult);
            os.close();
        } catch (FileNotFoundException e) {
            Throwables.propagate(e);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        
        return storedEquivalenceResult;
    }

    @Override
    public StoredEquivalenceResult forId(Id id) {
        File file = fileFromCanonicalUri(id.toString());
        if(!file.exists()) {
            return null;
        }
        
        ObjectInputStream is = null;
        boolean threw = true;
        try {
            is = new ObjectInputStream(new FileInputStream(file));
            StoredEquivalenceResult readObject = (StoredEquivalenceResult) is.readObject();
            threw = false;
            return readObject;
        } catch (Exception e) {
            Throwables.propagate(e);
        } finally {
            try {
                Closeables.close(is, threw);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        return null;
    }

    @Override
    public List<StoredEquivalenceResult> forIds(Iterable<Id> ids) {
        return Lists.newArrayList(Iterables.transform(ids, new Function<Id, StoredEquivalenceResult>() {

            @Override
            public StoredEquivalenceResult apply(Id input) {
                return forId(input);
            }
            
        }));
    }
    
    private File fileFromCanonicalUri(String canonicalUri) {
        return new File(directory, canonicalUri.replace('/', '-'));
    }

}
