/*
 * Copyright (c) 2016 Christian Garbers.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Simplified BSD License
 *  which accompanies this distribution
 *  Contributors:
 *       Christian Garbers - initial API and implementation
 */

$(document).ready(function(){
	$ = jQuery.noConflict();
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
	$("#root").on('focusout',"#BuyKarma_textfield", buyKarma);
	$("#root").on('focusout',"#SpentKarma_textfield", spentKarma);
	$("#root").on('focusout',"#AddLP_textfield", addLP);
});

function addLP(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/lp/add/" + $("#AddLP_textfield").val() + "/", reload_char)
		.fail(failedQuery);
	}

function buyKarma(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/karma/buy/" + $("#BuyKarma_textfield").val() + "/", reload_char)
		.fail(failedQuery);
	}

function spentKarma(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/karma/spent/" + $("#SpentKarma_textfield").val() + "/", reload_char)
		.fail(failedQuery);
	}

function shareChar(event){
	$.post("/API/characters/share/" + event.target.id + "/" + $("#share_textfield").val() + "/", reload_chars)
		.fail(failedQuery);
	}

function getSharePage(event){
	$("#roller").load("/HTML/share/"+event.target.id+"/",	
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
	fade();
	$.get("/API/characters/" + $("#char_id").text() + "/armors/" + event.target.id + "/thread/attach/", reload_char)
		.fail(failedQuery);
	}
	
function threadDown(event){
	fade();
	$.get("/API/characters/" + $("#char_id").text() + "/armors/" + event.target.id + "/thread/remove/", reload_char)
		.fail(failedQuery);
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

function skillDown(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/skills/" + event.target.id + "/corrupt/", reload_char)
		.fail(failedQuery);
	}

function skillUp(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/skills/" + event.target.id + "/improve/", reload_char)
		.fail(failedQuery);
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

function disciplineUp(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/disciplines/" + event.target.id + "/improve/", reload_char)
		.fail(failedQuery);
	}

function disciplineDown(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/disciplines/" + event.target.id + "/corrupt/", reload_char)
		.fail(failedQuery);
	}

function fetchRaceBlock(){
	$("#race").load("/HTML/races/");
	}

function changeRace(event){
	$.post("/API/characters/" + $("#char_id").text() + "/race/" + event.target.id + "/", reload_char)
		.fail(failedQuery);
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
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/talents/" + event.target.id + "/improve/", reload_char)
		.fail(failedQuery);
	}

function talentDown(event) {
	fade();
	$.post("/API/characters/"+$("#char_id").text()+"/talents/"+event.target.id+"/corrupt/",reload_char);
	}

function attributeUp(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/attributes/" + event.target.id + "/improve/", reload_char)
		.fail(failedQuery);
	}

function attributeDown(event) {
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/attributes/" + event.target.id + "/corrupt/", reload_char)
		.fail(failedQuery)
	}

function attributePPUp(event){
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/attributes/" + event.target.id + "/improve/pp/", reload_char)
		.fail(failedQuery);
	}
	
function attributePPDown(event){
	fade();
	$.post("/API/characters/" + $("#char_id").text() + "/attributes/" + event.target.id + "/corrupt/pp/", reload_char)
		.fail(failedQuery);
	}

function reload_talents(event){	
	$("#talents").load("/HTML/char/"+$("#char_id").text()+"1/talents/");
	}

function fade() {
	$.blockUI();
	//$("#char_root").fadeTo("fast", .5);
}

function blendIn() {
	$.unblockUI();
	$("#char_root").fadeTo("fast'", 1.);
}

function reload_char(event) {
	$("#float_elem").fadeOut("fast");
	$("#roller").fadeOut("fast");
	$("#char_root").load("/HTML/char/" + $("#char_id").text() + "/" + $.now() + "/", blendIn);
	$("#float_elem").empty();
	}
	
function reload_chars(event){	
	$("#chars_root").fadeIn("normal");
	$("#float_elem").fadeOut("normal");
	$("#chars_root").load("/HTML/chars/");
}
function failedQuery(qXHR, textStatus, errorThrown) {
	$.blockUI({
		message: "<h1>This did not work. The querry failed with " + errorThrown + qXHR.responseText + "</h1>",
		css: {color: '#FF0000'}
	});
	setTimeout(function () {
			reload_char(qXHR);
		},
		2000);

}