/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.identity.entityasidentity2;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "EAI2Attendance")
@IdClass(Attendance.AttendanceId.class)
public class Attendance {

    public static class AttendanceId {

        private int student;
        private int course;

        public AttendanceId() {}

        public AttendanceId(int studentId, int courseId) {
            this.student = studentId;
            this.course = courseId;
        }

        public String toString() {
            return student + ":" + course;
        }

        public int hashCode() {
            return (17 + student) * 37 + course;
        }

        public boolean equals(Object other) {
            return this == other
                || other instanceof AttendanceId
                    && student == ((AttendanceId) other).student
                    && course == ((AttendanceId) other).course;
        }
    }

    @Id @ManyToOne
    Student student;

    @Id @ManyToOne
    Course course;
}
