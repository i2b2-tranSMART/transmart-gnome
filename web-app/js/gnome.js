var gnomeCss =
	'<style>' +
	'#gnomeTable th {' +
		'font: 16px "Helvetica",arial,clean,sans-serif;' +
	'}\n' +
	'#gnomeTable td, #gnomeTable th {' +
		'text-align: center;' +
		'vertical-align: middle;' +
		'padding: 20px;' +
	'}\n' +
	'.gnomeButton {' +
		'background-color: #f5f5f5;' +
		'border-radius: 2px 0 0 0;' +
		'border: 1px solid #ccc;' +
		'box-shadow: -1px -1px 2px rgba(0, 0, 0, 0.5);' +
		'color: #444 !important;' +
		'font: 14px "Helvetica",arial,clean,sans-serif;' +
		'padding: 6px 20px 6px 20px;' +
		'text-decoration: none !important;' +
		'white-space: nowrap;' +
	'}\n' +
	'</style>\n';

function gnomeBootstrap(targetPanel) {
	$j.ajax({
		url: pageInfo.basePath + '/gnome/projectNames',
		success: function(projectNamesJson) {
			if (projectNamesJson.length !== 0) {
				createGnomePanel(targetPanel, projectNamesJson);
			}
		}
	});
}

function createGnomePanel(targetPanel, projectNames) {

	var buttonRows = '';
	for (var i = 0; i < projectNames.length; i++) {
		var element = projectNames[i];
		buttonRows +=
			'<tr>' +
			'<td><a href="#" class="gnomeButton" onclick="gnomeRequest(\'' + element + '\', \'genes\')">' + element + '</a></td>' +
			'<td><a href="#" class="gnomeButton" onclick="gnomeRequest(\'' + element + '\', \'variants\')">' + element + '</a></td>' +
			'</tr>';
	}

	targetPanel.add(new Ext.Panel({
		id: 'gnomePanel',
		title: 'gNOME',
		region: 'north',
		split: true,
		height: 340,
		layout: 'fit',
		autoScroll: true,
		html: gnomeCss + '<table id="gnomeTable"><tr><th>Analyze Genes</th><th>Analyze Variants</th></tr>' + buttonRows + '</table>',
		collapsible: true
	}));
}

function gnomeRequest(project, type) {

	GLOBAL.CurrentSubsetIDs[1] = null;
	GLOBAL.CurrentSubsetIDs[2] = null;

	generatePatientCohort(function () {
		var queryPanel = Ext.getCmp('queryPanel');
		$j.ajax({
			url: pageInfo.basePath + '/gnome/analyzegnomeapi',
			data: { type: type, project: project, result_instance_id: GLOBAL.CurrentSubsetIDs[1]},
			beforeSend: function() {
				queryPanel.el.mask('Querying gNOME...', 'x-mask-loading');
			},
			success: function(data) {
				window.open(data);
			},
			error: function(xhr) {
				showFailureMessage(xhr);
			},
			complete: function() {
				queryPanel.el.unmask();
			}
		});
	});
}

function generatePatientCohort(callback) {
	if (areAllSubsetsEmpty()) {
		return false;
	}

	determineNumberOfSubsets();

	for (var i = 1; i <= GLOBAL.NumOfSubsets; i++) {
		if (!isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null) {
			runQuery(i, callback);
		}
	}
}

function showFailureMessage(xhr) {
	var defaultMsg = 'Failed to perform operation';
	var msg = xhr.responseText;
	if (!msg || msg.length === 0){
		msg = defaultMsg;
	}
	alert(msg);
}
