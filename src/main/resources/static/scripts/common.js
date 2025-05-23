
function confirmDeleteItem(item) {
    console.log("confirmDeleteItem "+item)
	$("#deleteId").text(item);
    $('#deleteModal').modal('show');
}
