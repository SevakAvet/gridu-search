package com.gridu.search.solr;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import java.io.IOException;

public class FieldWordNumberUpdateProcessor extends UpdateRequestProcessor {
    private final String TARGET_FIELD_NAME_PREFIX = "number_of_words_in_";
    private final String targetField;

    public FieldWordNumberUpdateProcessor(String targetField, UpdateRequestProcessor next) {
        super(next);
        this.targetField = targetField;
    }

    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        SolrInputDocument doc = cmd.getSolrInputDocument();
        SolrInputField targetField = doc.get(this.targetField);

        String value = (String) targetField.getValue();
        int wordsCount = value.trim().split("\\W+").length;

        doc.addField(TARGET_FIELD_NAME_PREFIX + this.targetField, wordsCount);
        super.processAdd(cmd);
    }
}
