$(document).ready(function(){
	$.ajaxSetup({ cache: false });
	$("#root").on('focusout',"#name", changeCharName);
	$("#root").on('click',".talent_up",talentUp);
	$("#root").on('click',".talent_down",talentDown);
	$("#root").on('click',".attribute_down",attributeDown);
	$("#root").on('click',".attribute_pp_up",attributePPUp);
	$("#root").on('click',".attribute_pp_down",attributePPDown);
	$("#root").on('click',".attribute_up",attributeUp);
	$("#root").on('click',"#race_name",fetchRaceBlock);
	$("#root").on('click',".race_chosen",changeRace);
	$("#root").on('click',".discipline_up",disciplineUp);
	$("#root").on('click',".discipline_down",disciplineDown);
	$("#root").on('click',"#discipline_heading",fetchDisciplinesBlock);
	$("#root").on('click',".discipline_chosen",learnDiscipline);
	$("#root").on('click',"#reload_char",reload_char);
	$("#root").on('click',"#skills_heading",fetchSkillsBlock);
	$("#root").on('click',"#spells_heading",fetchSpellsBlock);
	$("#root").on('click',".skill_chosen",skillUp);
	$("#root").on('click',".skill_up",skillUp);
	$("#root").on('click',".skill_down",skillDown);
	$("#root").on('click',".spell_chosen",learnSpell);
	$("#root").on('click',".unlearn_spell",unlearnSpell);
	$("#root").on('click',"#armors_heading",fetchArmorsBlock);
	$("#root").on('click',"#armors_heading",fetchArmorsBlock);
	$("#root").on('click',".remove_armor",removeArmor);
	$("#root").on('click',".armor_chosen",getArmor);
	$("#root").on('click',".thread_up",threadUp);
	$("#root").on('click',".thread_down",threadDown);
	$("#root").on('click',"#bla", sandbox);
	$("#root").on('click',".chars_row", redirectChar);
	$("#root").on('click',"#createChar", createChar);
	$("#root").on('click',"[class*='dice']", getDicePage);
	$("#root").on("click",".cast_spell", getSpellPage);
	$("#root").on("click",".waeve_thread", weaveThread);	
	$("#root").on('click',"[id*='dice']", getDicePage);
	$("#root").on("click",".matrix", getMatrixPage);	
	$("#root").on("click","#throw_wuerfel", throwDice);
	$("#root").on("click",".nomatrix", add2Matrix);
	$("#root").on("click",".inmatrix", removeFromMatrix);
	$("#root").on("click",".share_char", getSharePage);
	$("#root").on("mouseenter",".discipline_name", showAbilities);
	$("#root").on("mouseleave",".discipline_abilities_vis", hideAbilities);
	$("#root").on("click",".delete_char", deleteChar);
	$("#root").on("click",".share_now", shareChar);	
});

function shareChar(event){
	$.post("/API/characters/share/"+event.target.id+"/"+$("#share_textfield").val()+"/",reload_chars);
	}

function getSharePage(event){
	$("#roller").load("/HTML/char	/share/"+event.target.id+"/",	
	function(){	
	$("#roller").css({ top: $(event.target).position().top, left: $(event.target).position().left});
	$("#roller").fadeIn("normal");});
	}
function deleteChar(event){
	$.post("/API/characters/delete/"+event.target.id+"/",reload_chars);
	}

function showAbilities(event){
	id = event.target.id.split("_")[0];
	$("#"+id+"_abilities").css({"top":$(event.target).position().top+"px" ,"left": $(event.target).position().left+"px"});	
	$("#"+id+"_abilities").switchClass("discipline_abilities_inv","discipline_abilities_vis",0);
	}

function hideAbilities(event){
	$(".discipline_abilities_vis").switchClass("discipline_abilities_vis","discipline_abilities_inv",0);
	}


function add2Matrix(event){
		$.ajax({
		url:"/API/characters/"+$("#char_id").text()+"/addmatrix/"+event.target.id+"/",
		type:"POST"
		});
		$(event.target).switchClass("nomatrix","inmatrix");
	}

function removeFromMatrix(event){
		$.ajax({
		url:"/API/characters/"+$("#char_id").text()+"/frommatrix/"+event.target.id+"/",
		type:"POST"
		});
		$(event.target).switchClass("inmatrix","nomatrix");
	}

function getMatrixPage(event){
	$("#roller").load("/HTML/matrices/"+$("#char_id").text()+"/"+event.target.id+"/",	
	function(){	
	$("#roller").css({ top: $(event.target).position().top, left: $(event.target).position().left});
	$("#roller").fadeIn("normal");});
	}
	
function weaveThread(event){
	$("#"+event.target.id+"Result").empty();
		$("#"+event.target.id+"Result").load("/API/dices/"+$("#"+event.target.id+"_textfield").val()+"/");
	}
	
function getSpellPage(event){
	$("#roller").load("/HTML/spell/"+event.target.id+"/"+$("#char_id").text()+"/",	
	function(){	
	$("#roller").css({ top: $(event.target).position().top, left: $(event.target).position().left})	;
	$("#roller").fadeIn("normal");});
	}

function throwDice(event){
		$("#dice_result").empty();
		$("#dice_result").load("/API/dices/"+$("#wuerfel_textfield").val()+"/");
	}
	
function getDicePage(event){
	$("#roller").load("/HTML/dice/"+event.target.className+"/"+event.target.id+"/"+event.target.textContent+"/"+$("#char_id").text()+"/",
	function(){	
	$("#roller").css({ top: $(event.target).position().top, left: $(event.target).position().left});
	$("#roller").fadeIn("normal");});
	}

function createChar(event){
		$.ajax({
		url:"/API/characters/create/",
		type:"POST",
		success:function(msg){
				window.location.replace("/char/"+msg+"/");			
			}
		});
	}

function redirectChar(event){
		window.location.href = "/char/"+event.target.id+"/";
	}

function sandbox(event){
		$("#char_root").fadeOut("normal");
	}

function threadUp(event){
	$.get("/API/characters/"+$("#char_id").text()+"/armors/"+event.target.id+"/thread/attach/",reload_char);
	}
	
function threadDown(event){
	$.get("/API/characters/"+$("#char_id").text()+"/armors/"+event.target.id+"/thread/remove/",reload_char);
	}

function getArmor(event){
	$.get("/API/characters/"+$("#char_id").text()+"/armors/"+event.target.id+"/get/",reload_char);
	}
	
function removeArmor(event){
	$.get("/API/characters/"+$("#char_id").text()+"/armors/"+event.target.id+"/remove/",reload_char);
	}
	
function learnSpell(event){
	$.post("/API/characters/"+$("#char_id").text()+"/spells/"+event.target.id+"/learn/",reload_char);
	}

function unlearnSpell(event){
	$.post("/API/characters/"+$("#char_id").text()+"/spells/"+event.target.id+"/unlearn/",reload_char);
	}

function skillDown(event){	
	$.post("/API/characters/"+$("#char_id").text()+"/skills/"+event.target.id+"/corrupt/",reload_char);
	}
	
function skillUp(event){	
	$.post("/API/characters/"+$("#char_id").text()+"/skills/"+event.target.id+"/improve/",reload_char);
	}

function fetchSkillsBlock(){
	$("#char_root").fadeOut("normal");
	$("#float_elem").load("/HTML/skills/");
	window.scrollTo(0, 0);
	$("#float_elem").fadeIn("normal");
	}

function fetchSpellsBlock(){
	$("#char_root").fadeOut("normal");
	$("#float_elem").load("/HTML/spells/char/"+$("#char_id").text()+"/");
	window.scrollTo(0, 0);
	$("#float_elem").fadeIn("normal");	
	}

function learnDiscipline(event){
	$.post("/API/characters/"+$("#char_id").text()+"/disciplines/"+event.target.id+"/improve/",reload_char);
	}
	
function fetchDisciplinesBlock(){
	$("#char_root").fadeOut("normal");
	$("#float_elem").load("/HTML/disciplines/");
	window.scrollTo(0, 0);
	$("#float_elem").fadeIn("normal");	
	}
	
function fetchArmorsBlock(){
	$("#char_root").fadeOut("normal");
	$("#float_elem").load("/HTML/armors/");
	window.scrollTo(0, 0);
	$("#float_elem").fadeIn("normal");
	}
	
function disciplineUp(event){	
	$.post("/API/characters/"+$("#char_id").text()+"/disciplines/"+event.target.id+"/improve/",reload_char);
	}
	
function disciplineDown(event){	
	$.post("/API/characters/"+$("#char_id").text()+"/disciplines/"+event.target.id+"/corrupt/",reload_char);
	}

function fetchRaceBlock(){
	$("#race").load("/HTML/races/");
	}

function changeRace(event){
	$.post("/API/characters/"+$("#char_id").text()+"/race/"+event.target.id+"/",reload_char);
	}
	

function changeCharName(){
	$.ajax({
		url:"/API/characters/"+$("#char_id").text()+"/name/",
		type:"POST",
		data:JSON.stringify({name:$("#name").val()}),
		dataType:"json",
		contentType: "application/json"
		});
	}
	
function talentUp(event){
	
	$.post("/API/characters/"+$("#char_id").text()+"/talents/"+event.target.id+"/improve/",reload_char);
	}
	
function talentDown(event){
	
	$.post("/API/characters/"+$("#char_id").text()+"/talents/"+event.target.id+"/corrupt/",reload_char);
	}
	
function attributeUp(event){
	
	$.post("/API/characters/"+$("#char_id").text()+"/attributes/"+event.target.id+"/improve/",reload_char);
	}
	
function attributeDown(event){	
	$.post("/API/characters/"+$("#char_id").text()+"/attributes/"+event.target.id+"/corrupt/",reload_char);
	}

function attributePPUp(event){
	$.post("/API/characters/"+$("#char_id").text()+"/attributes/"+event.target.id+"/improve/pp/",reload_char);
	}
	
function attributePPDown(event){
	$.post("/API/characters/"+$("#char_id").text()+"/attributes/"+event.target.id+"/corrupt/pp/",reload_char);
	}

function reload_talents(event){	
	$("#talents").load("/HTML/char/"+$("#char_id").text()+"1/talents/");
	}
	
function reload_char(event){	
	$("#char_root").fadeIn("normal");
	$("#float_elem").fadeOut("normal");
	$("#roller").fadeOut("normal");
	$("#char_root").load("/HTML/char/"+$("#char_id").text()+"/");
	$("#float_elem").empty();
	}
	
function reload_chars(event){	
	$("#chars_root").fadeIn("normal");
	$("#float_elem").fadeOut("normal");
	$("#chars_root").load("/HTML/chars/");
	}