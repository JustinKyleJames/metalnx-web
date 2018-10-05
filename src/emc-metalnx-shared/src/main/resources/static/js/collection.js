function getCollectionSummary(path){
	window.location.href = '/emc-metalnx-web/collectionInfo?path=' + path; //relative to domain
}

function displayCollectionSummary(data){
	$("#summary").html(data);
}

function getInfoDetails(path){
	var url = "/emc-metalnx-web/collectionInfo/collectionFileInfo/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
}

function getMetadata(path){
	console.log("Collection getMetadata() :: " +path);
	var url = "/emc-metalnx-web/metadata/getMetadata/";
	ajaxEncapsulation(url , "POST", {path: path}, displayMetadata, null, null, null);
}

function getPermissionDetails(path){
	console.log("Collection getPermDetails() :: " +path);
	var url = "/emc-metalnx-web/permissions/getPermissionDetails/";
	ajaxEncapsulation(url, "POST", {path: path}, displayPermissionDetails, null, null);
}

function getPerview(path){
	console.log("Collection getPreview() :: " + path)
	var url = "/emc-metalnx-web/previewPreparation/";
	ajaxEncapsulation(url, "GET", {path:path}, displayPreviewImage, null, null);
}

function displayInfoDetails(data){
	$("#details").html(data);
}

function displayMetadata(data){
	/*$("#table-loader").hide();
	$("#metadata").show();*/
	$("#metadata").html(data);
}

function displayPermissionDetails(data){
	/*$(".loading").hide();
	$("#permission").show();*/
	$('#permission').html(data);
}

function displayPreviewImage(data){
	$("#preview").html(data);
}


function starPath(path){
	console.log("StarPath() starts");
	//$('#breadcrumbStar').attr('onclick', '');
	var url = "/emc-metalnx-web/favorites/addFavoriteToUser/";
	ajaxEncapsulation(url, "GET", {path: path},
			function(data){
		if(data.indexOf("OK") >= 0){
			$('#breadcrumbStar i').removeClass('bm-unchecked').addClass('bm-checked');
			$('#breadcrumbStar').attr('onclick', 'unstarPath("'+encodeURI(path)+'")');
			//$('#breadcrumbStar').tooltip('hide').attr('data-original-title',[[#{collections.favorite.unmark.button.tooltip}]]);
		}else{
			$('#breadcrumbStar').attr('data-content', 'Could not add path to favorites.')
			$('#breadcrumbStar').popover("show");
			$('#breadcrumbStar').attr('onclick', 'starPath("'+encodeURI(path)+'")');
		}

	}, null, null, null);
	console.log("StarPath() ends");
}

function unstarPath(path){
	console.log("UnstarPath() starts !!");
	//$('#breadcrumbStar').attr('onclick', '');
	var url = "/emc-metalnx-web/favorites/removeFavoriteFromUser/";
	ajaxEncapsulation(url, "GET", {path: path},
		function(data){
			if(data.indexOf("OK") >= 0){
				$('#breadcrumbStar i').removeClass('bm-checked').addClass('bm-unchecked');
				$('#breadcrumbStar').attr('onclick', 'starPath("'+encodeURI(path)+'")');
				//$('#breadcrumbStar').tooltip('hide').attr('data-original-title',[[#{collections.favorite.button.tooltip}]]);
			}else{
				$('#breadcrumbStar').attr('data-content', 'Could not remove path from favorites.')
				$('#breadcrumbStar').popover("show");
				$('#breadcrumbStar').attr('onclick', 'unstarPath("'+encodeURI(path)+'")');
			}
		}, null, null, null);
	console.log("UnstarPath() ends");
}

function positionBrowserToPath(path) {
	console.log("positionBrowserToPath()");
	window.location.href = '/emc-metalnx-web/collections?path=' + encodeURI(path); //relative to domain
}

function fileDownload(path){
	$("#breadcrumDownloadBtn").attr('disabled','disabled');
	$("#actionsWait").show();
	//$('#actionLabel').html([[#{collections.management.progress.label.download}]]);
	$('#actionLabel').text($("#container").data("msg-txt"));
	var prepareDownloadURL = "/emc-metalnx-web/fileOperation/prepareFilesForDownload/";
	var paths = [];
	paths.push(path);
	ajaxEncapsulation(prepareDownloadURL, "GET", {paths: paths}, handleDownload, null);
}

function showInheritanceAction() {
	$("#updateInheritanceModal").modal("show");
}


function updateInheritanceNonRecursive(path) {
	updateInheritance(path, false);
}


function updateInheritanceRecursive(path) {
	updateInheritance(path, true);
}


function updateInheritance(path, isRecursive) {
	console.log("updateInheritance()");
	console.log("path:" + path);
	console.log("recursive:" + isRecursive);




-------- here!!!!! -----

	var inheritanceValue = !($('#').is(':checked'));

	setOperationInProgress();
	toastr["success"]("Operation successful", "inheritance value was updated successfully");
	unsetOperationInProgress();
}


function deleteInfoAction(path){
	setOperationInProgress();
	console.log("Ready for deletion");
	$("#actionmenu button").prop("disabled", true);
	$('#actionsWait').show();

	var paths = [];
	paths.push(path);
	var url = "/emc-metalnx-web/fileOperation/delete/";

	ajaxEncapsulation(
			url,
			"POST",
			{paths: paths},
			function (data) {
				unsetOperationInProgress();
				$('#actionsWait').hide();
				$('#deleteConfirmationModal').modal();
			}
	);
	$("#deleteModal").modal("hide");
	cleanModals();
}

function cleanModals() {
	$(".modal").modal("hide");

	//reset the modal
	$(".modal a").each(function(){
		if($(this).attr('onclick') !== undefined){
			if($(this).attr('onclick').indexOf('retractElement') >= 0){
				eval($(this).attr('onclick'));
			}
		}
	});
}

function editInfo(path){

}

function handleDownload(data) {
	console.log("collection.js :: success call :: handleDownload()")
	if (data.downloadLimitStatus == "ok"){
		window.location.href = "/emc-metalnx-web/fileOperation/download/";
		$("#breadcrumDownloadBtn").removeAttr("disabled");
		$("#actionsWait").hide();
	}
	else {
		toastr.warning("Download limit has been exceeded over 100MBs");
		$("#actionsWait").hide();
	}
}

function setOperationInProgress() {
	operationInProgress = true;
}

function unsetOperationInProgress() {
	cleanModals();
	operationInProgress = false;
}

