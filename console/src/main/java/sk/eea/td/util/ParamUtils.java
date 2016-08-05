package sk.eea.td.util;

import sk.eea.td.console.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParamUtils {

    public static void copyParamsIntoJobRun(Set<Param> params, AbstractJobRun jobRun) {
        for (Param param : params) {
            if(param instanceof StringParam) {
                jobRun.addReadOnlyParam(new StringReadOnlyParam((StringParam) param, jobRun));
            } else if(param instanceof BlobParam) {
                jobRun.addReadOnlyParam(new BlobReadOnlyParam((BlobParam) param, jobRun));
            }
        }
    }

    public static Map<ParamKey, String> copyStringParamsIntoStringParamMap(Set<Param> paramList) {
        final Map<ParamKey, String> map = new HashMap<>();
        paramList.stream().filter(p -> p instanceof StringParam).forEach(p -> map.put(p.getKey(), ((StringParam) p).getStringValue()));
        return map;
    }

    public static Map<ParamKey, BlobParam> copyBlobParamsIntobBlobParamMap(Set<Param> paramList) {
        final Map<ParamKey, BlobParam> map = new HashMap<>();
        paramList.stream().filter(p -> p instanceof BlobParam).forEach(p -> map.put(p.getKey(), (BlobParam) p));
        return map;
    }

    public static Map<ParamKey, String> copyStringReadOnLyParamsIntoStringParamMap(Set<ReadOnlyParam<?>> paramList) {
        final Map<ParamKey, String> map = new HashMap<>();
        paramList.stream().filter(p -> p instanceof StringReadOnlyParam).forEach(p -> map.put(p.getKey(), ((StringReadOnlyParam) p).getStringValue()));
        return map;
    }

    public static Map<ParamKey, BlobReadOnlyParam> copyBlobReadOnlyParamsBlobParamMap(Set<ReadOnlyParam<?>> paramList) {
        final Map<ParamKey, BlobReadOnlyParam> map = new HashMap<>();
        paramList.stream().filter(p -> p instanceof BlobReadOnlyParam).forEach(p -> map.put(p.getKey(), (BlobReadOnlyParam) p));
        return map;
    }
}
