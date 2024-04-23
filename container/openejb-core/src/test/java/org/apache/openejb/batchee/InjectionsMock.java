/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.batchee;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.api.BatchProperty;
import jakarta.batch.runtime.context.JobContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.Assert;

import java.net.URL;

@Named("injected")
public class InjectionsMock extends AbstractBatchlet {
    @Inject
    @BatchProperty
    private URL url;

    @Inject
    private JobContext jobContext;

    @Inject
    private BeanManager beanManager;

    @Override
    public String process() throws Exception {
        Assert.assertTrue(url.toExternalForm().equals("http://batchee.incubator.org"));
        Assert.assertNotNull(jobContext);
        Assert.assertEquals(jobContext.getJobName(), "injections");

        // now check whether injection also works during the runtime of the batch
        // this is needed if you e.g have a NormalScoped bean which only now gets initialized.
        Assert.assertNotNull(beanManager);
        Bean<?> bean = beanManager.resolve(beanManager.getBeans(JobContext.class));
        JobContext jc = (JobContext) beanManager.getReference(bean, JobContext.class, beanManager.createCreationalContext(bean));
        Assert.assertNotNull(jc);
        Assert.assertEquals(jc.getJobName(), "injections");

        return "true";
    }
}
