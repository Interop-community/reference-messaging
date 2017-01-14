package org.hspconsortium.platform.messaging.converter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

// todo promote this to some common library
public class HapiJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object>
        implements GenericHttpMessageConverter<Object> {

    private static final FhirContext FHIR_CONTEXT = FhirContext.forDstu3();

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return canRead(clazz, null, mediaType);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        boolean canRead = true;

        // can only read application/json
        if (mediaType != null) {
            canRead = mediaType.isCompatibleWith(MediaType.APPLICATION_JSON);
        }

        return canRead;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        boolean canWrite = true;

        // can only read application/json
        if (mediaType != null) {
            canWrite = mediaType.isCompatibleWith(MediaType.APPLICATION_JSON);
        }

        return canWrite;
    }

    @Override
    public boolean canWrite(Type type, Class<?> aClass, MediaType mediaType) {
        return canWrite(aClass, mediaType);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // should not be called, since we override canRead/Write instead
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        String stringBody = IOUtils.toString(inputMessage.getBody(), DEFAULT_CHARSET);
        return (IResource) FHIR_CONTEXT.newJsonParser().parseResource(stringBody);
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // todo not right implementation, does not use contextClass
        String stringBody = IOUtils.toString(inputMessage.getBody(), DEFAULT_CHARSET);
        return (IResource) FHIR_CONTEXT.newJsonParser().parseResource(stringBody);
    }

    @Override
    public void write(Object o, Type type, MediaType mediaType, HttpOutputMessage httpOutputMessage)
            throws IOException, HttpMessageNotWritableException {
        writeInternal(o, httpOutputMessage);
    }


    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        if (object != null) {
            try {
                String json = FHIR_CONTEXT.newJsonParser().encodeResourceToString((IBaseResource) object);
                outputMessage.getBody().write(json.getBytes(DEFAULT_CHARSET));
            } catch (ClassCastException e) {
                throw new HttpMessageNotWritableException(e.getMessage(), e);
            }
        } else {
            outputMessage.getBody().close();
        }
    }

    @Override
    protected MediaType getDefaultContentType(Object object) throws IOException {
        return MediaType.APPLICATION_JSON;
    }

    @Override
    protected Long getContentLength(Object object, MediaType contentType) throws IOException {
        return null;
    }

}
