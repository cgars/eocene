import org.scalatestplus.play._
import org.scalatest.mock._
import org.mockito.Mockito._
import models.Race
import anorm._
import play.libs.Json
import models._

class RaceSpecs extends PlaySpec with MockitoSugar{
	  "Race" must {
    "be convertable to json" in {
    	val test_race = Race(1, "TestName", 5, 5,5, 5, 5, 5, 5,10, 10, 5, 30,"Ab",10, 10,2, 3, 8,8)
    	val json_race = Json.toJson(test_race)
    	}
    }  
}

class ArmorSpecs extends PlaySpec with MockitoSugar{
  val mock_power = mock[Power]
  val test_armor = Armor(1, "test", 1, 1, 2, 3,Some(1), List(mock_power), None)
	  "Armor" must {
	  	"be convertable to json" in {    	
	  		val json_armor = Json.toJson(test_armor)
    	}
    "return correct armor values" in{      
    	assert(test_armor .getPhysicalArmor===2)
    	assert(test_armor .getMysticalArmor===3)
    	assert(test_armor .getMysticalArmor!==5)
    	when(mock_power.effect).thenReturn("Physic Armor")
    	when(mock_power.value).thenReturn(3)
    	assert(test_armor .getPhysicalArmor===5)
     	when(mock_power.effect).thenReturn("Mystic Armor")
    	when(mock_power.value).thenReturn(2)
    	assert(test_armor .getMysticalArmor===5)
     	when(mock_power.effect).thenReturn("Initiative")
    	when(mock_power.value).thenReturn(2)
    	assert(test_armor .getInitiativeBonus===3)
    	}
    }  
}


class CharacterSpecs extends PlaySpec with MockitoSugar{
  val mock_race = mock[Race]
  val mock_disc = mock[Discipline]
  val test_char = Character(1, "name", 1, 1, 1, 1, 1, 1, 2, 2, 2, 2,2, 2, 100, 50,
  10, 10, mock_race, List(mock_disc), List(), List(), List(),List(), 
  Map("physDef"->8))  
	  "Character" must {
	  	"be convertable to json" in {    	
	  		val json_armor = Json.toJson(test_char)
    	}
    "return correct modifiers" in{      
    	when(mock_disc.getModifierValueByName("spellDef")).thenReturn(8)
    	assert(test_char .getModifierValueByName("spellDef").get===8)
    	}
    }  
}

class DisciplineSpecs extends PlaySpec with MockitoSugar{
  val mock_modifier = mock[Modifier]
  val test_disc = Discipline(1, "name", "",  None, List(mock_modifier ))
	  "Discipline" must {
	  	"be convertable to json" in {    	
	  		Json.toJson(test_disc)
    	}
    "return correct modifier values" in{      
    	test_disc .getModifierValueByName("spellDef")
    	when(mock_modifier.id).thenReturn(2)
    	when(mock_modifier.value).thenReturn(2)
    	assert(test_disc .getModifierValueByName("spellDef")===2)
    	}
    }  
}

class ModifierSpect extends PlaySpec with MockitoSugar{
  "A modifier" must {
    "return correct values" in {
    	val fakeRow = mock[anorm.Row]
    	when(fakeRow[Int]("disciplines_modifiers.id_modifier")) thenReturn 5
    	when(fakeRow[Int]("disciplines_modifiers.circle")) thenReturn 5
    	when(fakeRow[Int]("value")) thenReturn 3    	
    	val modi = Modifier.getModifierByRow(fakeRow)
    	modi.circle == 5
    	modi.id == 5
    	modi.value == 3
    }
  }
}


class PowerSpect extends PlaySpec with MockitoSugar{
  val test_power =  Power(2, 1, "", 1)
  "A Power" must {
    "be convertable to json" in {    	
	  		Json.toJson(test_power)
    	}
    "return correct values" in {
    	val fakeRow = mock[anorm.Row]
    	when(fakeRow[Int]("disciplines_modifiers.id_modifier")) thenReturn 5
    	when(fakeRow[Int]("disciplines_modifiers.circle")) thenReturn 5
    	when(fakeRow[Int]("value")) thenReturn 3    	
    	val modi = Modifier.getModifierByRow(fakeRow)
    	modi.circle == 5
    	modi.id == 5
    	modi.value == 3
    }
  }
}

class SkillSpect extends PlaySpec with MockitoSugar{
  val test_skill =  Skill(0, "", "Dex+1","","" ,Some(1))
  "A Skill" must {
    "be convertable to json" in {    	
	  		Json.toJson(test_skill)
    	}
    "return correct rank" in {
    	val fakeChar = mock[Character]
    	val my_map = Map("dex_step"->5,"t"->"sadad")
    	when(fakeChar.derived).thenReturn(my_map)
    	assert(test_skill.getRank(fakeChar)===7)

    }
  }
}