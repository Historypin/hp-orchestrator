package sk.eea.td.console.model;

public enum Flow {

    FLOW_1(Connector.EUROPEANA, Connector.HISTORYPIN),
    FLOW_2(Connector.HISTORYPIN, Connector.SD),
    FLOW_4(Connector.EUROPEANA_ANNOTATION, Connector.HISTORYPIN_ANNOTATION),
    FLOW_5(Connector.HISTORYPIN, Connector.MINT),
    FLOW_6(Connector.HISTORYPIN, Connector.TAGAPP);

    private Connector source;

    private Connector target;

    Flow(Connector source, Connector target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Returns flow with given source and target.
     *
     * @param source
     * @param target
     * @return flow or throws IllegalArgumentException if none matching flow is found.
     */
    public static Flow getFlow(Connector source, Connector target){
        for(Flow flow : values()) {
            if(flow.getSource().equals(source) && flow.getTarget().equals(target)) {
                return flow;
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find flow by given source: %s, and target: %s.", source, target));
    }

    public Connector getSource() {
        return source;
    }

    public void setSource(Connector source) {
        this.source = source;
    }

    public Connector getTarget() {
        return target;
    }

    public void setTarget(Connector target) {
        this.target = target;
    }
}
