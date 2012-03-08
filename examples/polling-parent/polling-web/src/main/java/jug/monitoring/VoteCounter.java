package jug.monitoring;

import jug.domain.Subject;

import javax.enterprise.context.ApplicationScoped;
import javax.management.Description;
import javax.management.MBean;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@MBean
@ApplicationScoped
@Description("count the number of vote by subject")
public class VoteCounter {
    private final Map<String, Subject> subjects = new ConcurrentHashMap<String, Subject>();

    @ManagedAttribute
    @Description("number of poll created/updated in this instance")
    public int getSubjectNumber() {
        return subjects.size();
    }

    @ManagedOperation
    @Description("current score of the specified poll")
    public String names() {
        return subjects.keySet().toString();
    }

    @ManagedOperation
    @Description("current score of the specified poll")
    public String score(final String name) {
        if (subjects.containsKey(name)) {
            return Integer.toString(subjects.get(name).score());
        }
        return "poll not found";
    }

    public void putSubject(final Subject subject) {
        subjects.put(subject.getName(), subject);
    }
}
