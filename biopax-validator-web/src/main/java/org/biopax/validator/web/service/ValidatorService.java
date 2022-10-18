package org.biopax.validator.web.service;


import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.normalizer.Normalizer;
import org.biopax.validator.BiopaxIdentifier;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Behavior;
import org.biopax.validator.api.beans.Validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

@Service
public class ValidatorService implements ValidatorAdapter {
  private final static DefaultResourceLoader LOADER = new DefaultResourceLoader();

  private Validator biopaxValidator; //to inject the biopax-validator
  private final String schema;

  @Autowired
  public ValidatorService(Validator biopaxValidator) throws IOException {
    this.biopaxValidator = biopaxValidator;
    this.schema = new String(FileCopyUtils.copyToByteArray(LOADER
      .getResource("classpath:validator-response-2.0.xsd").getInputStream()),"UTF-8");
  }

  public Validation validate(Resource data,
                             int errMax, boolean isFix,
                             Behavior errorLevel, String profile,
                             Normalizer normalizer) throws IOException
  {
    Validation validationResult = new Validation(new BiopaxIdentifier(),
      data.getDescription(), isFix, errorLevel, errMax, profile);
    //run the biopax-validator (this updates the validationResult object)
    biopaxValidator.importModel(validationResult, data.getInputStream());
    biopaxValidator.validate(validationResult);
    biopaxValidator.getResults().remove(validationResult);

    if(isFix) { // do normalize too
      if(normalizer == null) //e.g., when '/check' called from a client/script, not JSP
        normalizer = new Normalizer();

      Model m = (Model) validationResult.getModel();
      normalizer.normalize(m);//this further modifies the validated and auto-fixed model
      //update the serialized model (BioPAX RDF/XML)
      //for the client to get it (to possibly, unmarshall)
      validationResult.setModel(m);
      validationResult.setModelData(SimpleIOHandler.convertToOwl(m));
    } else {
      validationResult.setModelData(null);
      validationResult.setModel(null);
    }

    return validationResult;
  }

  public String getSchema() {
    return schema;
  }

}
