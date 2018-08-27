package com.gridu.search.solr;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

public class FieldWordNumberUpdateProcessorFactory extends UpdateRequestProcessorFactory {
    private String targetField;

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest solrQueryRequest, SolrQueryResponse solrQueryResponse, UpdateRequestProcessor next) {
        return new FieldWordNumberUpdateProcessor(targetField, next);
    }

    @Override
    public void init(NamedList args) {
        this.targetField = (String) args.get("targetField");
        super.init(args);
    }
}
