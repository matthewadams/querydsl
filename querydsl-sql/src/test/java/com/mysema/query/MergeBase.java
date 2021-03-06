/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.query;

import static com.mysema.query.Constants.survey;
import static com.mysema.query.Constants.survey2;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mysema.query.sql.dml.SQLMergeClause;
import com.mysema.query.sql.domain.QSurvey;
import com.mysema.testutil.IncludeIn;

import static com.mysema.query.Target.*;

public class MergeBase extends AbstractBaseTest{

    private void reset() throws SQLException{
        delete(survey).execute();
        insert(survey).values(1, "Hello World", "Hello").execute();
    }

    @Before
    public void setUp() throws SQLException{
        reset();
    }

    @After
    public void tearDown() throws SQLException{
        reset();
    }

    @Test
    @IncludeIn(H2)
    public void Merge_with_Keys_and_Values() {
        // NOTE : doesn't work with composite merge implementation
        // keys + values
        assertEquals(1, merge(survey).keys(survey.id).values(5, "Hello World", "Hello").execute());
    }

    @Test
    public void Merge_with_Keys_Columns_and_Values() {
        // keys + columns + values
        assertEquals(1, merge(survey).keys(survey.id)
            .set(survey.id, 5)
            .set(survey.name, "Hello World").execute());
    }
    
    @Test
    public void Merge_with_Keys_Columns_and_Values_using_null() {
        // keys + columns + values
        assertEquals(1, merge(survey).keys(survey.id)
            .set(survey.id, 5)
            .set(survey.name, (String)null).execute());
    }

    @Test
    @IncludeIn(H2)
    public void Merge_with_Keys_and_SubQuery() {    
        assertEquals(1, insert(survey).set(survey.id, 6).set(survey.name, "H").execute());

        // keys + subquery
        QSurvey survey2 = new QSurvey("survey2");
        assertEquals(2, merge(survey).keys(survey.id).select(
                sq().from(survey2).list(survey2.id.add(1), survey2.name, survey2.name2)).execute());
    }
    
    @Test
    @IncludeIn(H2)
    public void MergeBatch() {
        SQLMergeClause merge = merge(survey)
            .keys(survey.id)
            .set(survey.id, 5)
            .set(survey.name, "5")
            .addBatch();
        
        merge
            .keys(survey.id)
            .set(survey.id, 6)
            .set(survey.name, "6")
            .addBatch();
     
        assertEquals(2, merge.execute());
        
        assertEquals(1l, query().from(survey).where(survey.name.eq("5")).count());
        assertEquals(1l, query().from(survey).where(survey.name.eq("6")).count());
    }
    
    @Test
    @IncludeIn(H2)
    public void MergeBatch_with_subquery() {
        SQLMergeClause merge = merge(survey)
            .keys(survey.id)
            .columns(survey.id, survey.name)
            .select(sq().from(survey2).list(survey2.id.add(20), survey2.name))
            .addBatch();
        
        merge(survey)
            .keys(survey.id)
            .columns(survey.id, survey.name)
            .select(sq().from(survey2).list(survey2.id.add(40), survey2.name))
            .addBatch();
        
        merge.execute();
//        assertEquals(1, insert.execute());
    }
}
