package org.openmrs.ui.framework;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.fragment.FragmentActionUiUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class SimpleObjectTest {

    private UiUtils ui;

    @Before
    public void before() {

        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(null);

        this.ui = new FragmentActionUiUtils(null, null, null);
    }

    @Test
    public void fromObject_shouldCreateSimpleObject() {
        PersonName name = new PersonName();
        name.setGivenName("John");
        name.setMiddleName("J");
        name.setFamilyName("Johnson");

        String [] properties = {"givenName", "middleName", "familyName"};
        SimpleObject simpleName = SimpleObject.fromObject(name, ui, properties);

        Assert.assertEquals("John", simpleName.get("givenName"));
        Assert.assertEquals("J", simpleName.get("middleName"));
        Assert.assertEquals("Johnson", simpleName.get("familyName"));
    }

    @Test
    public void fromObject_shouldCreateSimpleObjectWithNestedProperties() {

        Person person = new Person();
		person.setPersonId(123);
        person.setGender("M");

        PersonName name = new PersonName();
        name.setGivenName("John");
        name.setMiddleName("J");
        name.setFamilyName("Johnson");
        name.setPreferred(true);
        person.addName(name);

        String [] properties = {"personId", "gender", "personName.givenName", "personName.middleName", "personName.familyName", "personName.preferred"};
        SimpleObject simplePerson = SimpleObject.fromObject(person, ui, properties);

		Assert.assertEquals(new Integer(123), simplePerson.get("personId"));
        Assert.assertEquals("M", simplePerson.get("gender"));

		Map<String,Object> nameMap = (Map<String,Object>) simplePerson.get("personName");

        Assert.assertEquals("John", nameMap.get("givenName"));
        Assert.assertEquals("J", nameMap.get("middleName"));
        Assert.assertEquals("Johnson", nameMap.get("familyName"));
		Assert.assertTrue((Boolean) nameMap.get("preferred"));
    }

    @Test
    public void fromObject_shouldNotFailIfParentNodeIsNull() {
        Person person = new Person();
        person.setGender("M");

        // we try to fetch names, even though there is no person name associated with this person
        String [] properties = {"gender", "personName.givenName", "personName.middleName", "personName.familyName"};
        SimpleObject simplePerson = SimpleObject.fromObject(person, ui, properties);

        Assert.assertEquals("M", simplePerson.get("gender"));
    }

    @Test
    public void fromObject_shouldFetchMapProperties()  {
        PersonAttributeType healthCenterType = new PersonAttributeType();
		healthCenterType.setPersonAttributeTypeId(1);
        healthCenterType.setName("Health Center");

        Person person = new Person();
        person.setGender("M");
        person.addAttribute(new PersonAttribute(healthCenterType,"Beth Israel"));

        String [] properties = {"gender", "personName.givenName","attributeMap.Health Center"};
        SimpleObject simplePerson = SimpleObject.fromObject(person, ui, properties);

        Assert.assertEquals("M", simplePerson.get("gender"));
        Assert.assertEquals("Beth Israel", ((Map<String,Object>) simplePerson.get("attributeMap")).get("Health Center"));
    }
}
