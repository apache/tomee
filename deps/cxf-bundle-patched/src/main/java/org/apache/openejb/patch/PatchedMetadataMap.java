package org.apache.openejb.patch;

import org.apache.cxf.jaxrs.impl.MetadataMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PatchedMetadataMap extends MetadataMap<String, String> {
    public PatchedMetadataMap(Map<String, List<String>> store, boolean ro, boolean ci) {
        super(store, ro, ci);
    }

    public Set<String> keySet() {
        Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(super.keySet());
        return set;
    }
}
