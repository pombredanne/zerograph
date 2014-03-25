package org.zerograph;

import org.zerograph.zap.api.ResourceInterface;

import java.util.HashMap;

public class ResourceSet {

    final private HashMap<String, ResourceInterface> resources;

    public ResourceSet() {
        this.resources = new HashMap<>();
    }

    public void add(ResourceInterface resource) {
        this.resources.put(resource.getName(), resource);
    }

    public boolean contains(String name) {
        return this.resources.containsKey(name);
    }

    public ResourceInterface get(String name) {
        return this.resources.get(name);
    }

}
