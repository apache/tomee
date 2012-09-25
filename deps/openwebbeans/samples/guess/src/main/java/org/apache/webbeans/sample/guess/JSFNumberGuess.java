/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.sample.guess;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

@Named(value = "game")
@SessionScoped
public class JSFNumberGuess implements Serializable
{
    private static final long serialVersionUID = 2264057893898002872L;

    private int no;
    private boolean correct = false;
    private int guess = 1;
    private int smallRange;
    private int maxRange;
    private int remainder;
    private @Inject @Default BeanManager manager;

    public JSFNumberGuess()
    {

    }

    @Inject
    public JSFNumberGuess(@NextNumber Integer number, @Highest Integer maxNumber)
    {
        this.no = number;
        this.smallRange = 1;
        this.maxRange = maxNumber;
        this.remainder = 10;
    }

    public String clear()
    {
        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<NextNumber>()
        {
        };

        Annotation[] anns2 = new Annotation[1];
        anns2[0] = new AnnotationLiteral<Highest>()
        {
        };

        Set<Bean<?>> beans = manager.getBeans(Integer.class, anns);
        Bean<?> bean = beans.iterator().next();
        this.no = (Integer)manager.getReference(bean, null, manager.createCreationalContext(bean));
        //this.no = manager.getInstanceByType(Integer.class, anns);
        setSmallRange(1);
        beans = manager.getBeans(Integer.class, anns2);
        bean = beans.iterator().next();
        setMaxRange((Integer)manager.getReference(bean, null, manager.createCreationalContext(bean)));
        //setMaxRange(manager.getInstanceByType(Integer.class, anns2));
        setRemainder(10);
        setGuess(1);
        setCorrect(false);

        return null;
    }

    public String checkNumber()
    {
        if (correct)
        {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Game is over! Please restart the game..."));
            return null;
        }

        if (guess > no)
        {
            maxRange = guess - 1;
        }
        if (guess < no)
        {
            smallRange = guess + 1;
        }
        if (guess == no)
        {
            correct = true;
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Correct! Please restart the game..."));

            return null;
        }

        if (remainder == 0)
        {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Game is over! Please restart the game..."));
            this.correct = false;

            return null;
        }
        else
        {
            remainder-=1;
        }

        return null;
    }

    /**
     * @return the no
     */
    public int getNo()
    {
        return no;
    }

    /**
     * @param no the no to set
     */
    public void setNo(int no)
    {
        this.no = no;
    }

    /**
     * @return the correct
     */
    public boolean isCorrect()
    {
        return correct;
    }

    /**
     * @param correct the correct to set
     */
    public void setCorrect(boolean correct)
    {
        this.correct = correct;
    }

    /**
     * @return the guess
     */
    public int getGuess()
    {
        return guess;
    }

    /**
     * @param guess the guess to set
     */
    public void setGuess(int guess)
    {
        this.guess = guess;
    }

    /**
     * @return the smallRange
     */
    public int getSmallRange()
    {
        return smallRange;
    }

    /**
     * @param smallRange the smallRange to set
     */
    public void setSmallRange(int smallRange)
    {
        this.smallRange = smallRange;
    }

    /**
     * @return the maxRange
     */
    public int getMaxRange()
    {
        return maxRange;
    }

    /**
     * @param maxRange the maxRange to set
     */
    public void setMaxRange(int maxRange)
    {
        this.maxRange = maxRange;
    }

    /**
     * @return the remainder
     */
    public int getRemainder()
    {
        return remainder;
    }

    /**
     * @param remainder the remainder to set
     */
    public void setRemainder(int remainder)
    {
        this.remainder = remainder;
    }

    /**
     * @return the manager
     */
    public BeanManager getManager()
    {
        return manager;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(BeanManager manager)
    {
        this.manager = manager;
    }

}
