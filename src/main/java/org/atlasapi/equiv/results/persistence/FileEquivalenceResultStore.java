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
import org.atlasapi.media.content.Content;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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
    public StoredEquivalenceResult forId(String canonicalUri) {
        File file = fileFromCanonicalUri(canonicalUri);
        if(!file.exists()) {
            return null;
        }
        
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
            return (StoredEquivalenceResult) is.readObject();
        } catch (IOException e) {
            Throwables.propagate(e);
        } catch (ClassNotFoundException e) {
            Throwables.propagate(e);
        }
        return null;
    }

    @Override
    public List<StoredEquivalenceResult> forIds(Iterable<String> canonicalUris) {
        return Lists.newArrayList(Iterables.transform(canonicalUris, new Function<String, StoredEquivalenceResult>() {

            @Override
            public StoredEquivalenceResult apply(String input) {
                return forId(input);
            }
            
        }));
    }
    
    private File fileFromCanonicalUri(String canonicalUri) {
        return new File(directory, canonicalUri.replace('/', '-'));
    }

}
