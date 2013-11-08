/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package openbook.domain;


/**
 * A simple numeric range.
 * Minimum value is included, maximum value is excluded.
 * 
 * @author Pinaki Poddar
 *
 */
public class Range<N extends Number> {
    private N min;
    private N max;
    private final Class<N> type;
    
    @SuppressWarnings("unchecked")
    public Range(Object min, Object max) {
        this((N)min, (N)max);
    }

    /**
     * Creates a range. Empty range i.e. where minimum equals maximum is allowed.
     * 
     * @param min non-null minimum value.
     * @param max non-null maximum value.
     */
    @SuppressWarnings("unchecked")
    public Range(N min, N max) {
        if (min == null || max == null)
            throw new IllegalArgumentException("Supplied Min or Max is null");
        if (max.doubleValue() < min.doubleValue())
            throw new IllegalArgumentException("Invalid range (" + min + "," + max + ")");
        this.min = min;
        this.max = max;
        type = (Class<N>)min.getClass();
    }
    
    public Class<N> type() {
        return type;
    }
    
    /**
     * Affirms if the given value is within this range.
     * Minimum is included, maximum is excluded.
     * 
     * @param x a non-null value
     * @return true if the given value is greater than or equals to minimum and less than the maximum.
     */
    public boolean contains(Number x) {
        return x != null && x.doubleValue() >= min.doubleValue() && x.doubleValue() < max.doubleValue();
    }
    
    /**
     * Affirms if the given range is within this range.
     * Minimum is included, maximum is excluded.
     * 
     * @param x a non-null value
     * @return true if the given value is greater than or equals to minimum and less than the maximum.
     */
    public <X extends Number> boolean contains(Range<X> r) {
        return r != null && r.getMinimum().doubleValue() >= min.doubleValue() && 
            r.getMaximum().doubleValue() <= max.doubleValue();
    }
    
    /**
     * Gets the minimum value.
     */
    public N getMinimum() {
        return min;
    }
    
    /**
     * Gets the maximum value.
     */
    public N getMaximum() {
        return max;
    }
    
    /**
     * Adjusts this range by the given number.
     * 
     * @param x a non-null value.
     * 
     * @return if this range is adjusted by this value.
     */
    public boolean adjust(N x) {
        if (x == null)
            return false;
        if (x.doubleValue() < min.doubleValue()) {
            min = x;
            return true;
        }
        if (x.doubleValue() > max.doubleValue()) {
            max = x;
            return true;
        }
        return false;
    }
    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((max == null) ? 0 : max.hashCode());
        result = prime * result + ((min == null) ? 0 : min.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Range other = (Range) obj;
        if (max == null) {
            if (other.max != null)
                return false;
        } else if (!max.equals(other.max))
            return false;
        if (min == null) {
            if (other.min != null)
                return false;
        } else if (!min.equals(other.min))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
