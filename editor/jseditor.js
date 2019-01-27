var container = document.getElementById('jsoneditor');

options = {
    mode: 'tree',
    modes: ['code', 'form', 'text', 'tree', 'view'], // allowed modes
    name: "jsonContent",
    onError: function (err) {
        alert(err.toString());
    },
    onChange: function () {
        console.log(editor.get());
        dm = editor.get();

        //we need to regenerate the graph part.
        //add a save button
        //use diff
        //then add and remove from cy

    }
};

var editor = new JSONEditor(container, options);