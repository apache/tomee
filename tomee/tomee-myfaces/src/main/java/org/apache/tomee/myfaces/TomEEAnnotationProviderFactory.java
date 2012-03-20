package org.apache.tomee.myfaces;

import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.myfaces.spi.AnnotationProviderFactory;
import org.apache.myfaces.spi.impl.DefaultAnnotationProviderFactory;

import javax.faces.context.ExternalContext;

public class TomEEAnnotationProviderFactory extends AnnotationProviderFactory {
    @Override
    public AnnotationProvider createAnnotationProvider(final ExternalContext externalContext) {
        AnnotationProvider annotationProvider = (AnnotationProvider) externalContext.getApplicationMap().get(DefaultAnnotationProviderFactory.ANNOTATION_PROVIDER_INSTANCE);
        if (annotationProvider == null) {
            annotationProvider = new TomEEAnnotationProvider();
            externalContext.getApplicationMap().put(DefaultAnnotationProviderFactory.ANNOTATION_PROVIDER_INSTANCE, annotationProvider);
        }
        return annotationProvider;
    }
}
