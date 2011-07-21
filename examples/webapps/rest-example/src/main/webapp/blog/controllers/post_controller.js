/**
 * @tag controllers, home
 * Displays a table of posts.	 Lets the user 
 * ["Blog.Controllers.Post.prototype.form submit" create], 
 * ["Blog.Controllers.Post.prototype.&#46;edit click" edit],
 * or ["Blog.Controllers.Post.prototype.&#46;destroy click" destroy] posts.
 */
$.Controller.extend('Blog.Controllers.Post',
/* @Static */
{
	onDocument: true
},
/* @Prototype */
{
 /**
 * When the page loads, gets all posts to be displayed.
 */
 "{window} load": function(){
	if(!$("#post").length){
	 $(document.body).append($('<div/>').attr('id','post'));
		 Blog.Models.Post.findAll({}, this.callback('list'));
 	}
 },
 /**
 * Displays a list of posts and the submit form.
 * @param {Array} posts An array of Blog.Models.Post objects.
 */
 list: function( posts ){
	$('#post').html(this.view('init', {posts:posts} ));
 },
 /**
 * Responds to the create form being submitted by creating a new Blog.Models.Post.
 * @param {jQuery} el A jQuery wrapped element.
 * @param {Event} ev A jQuery event whose default action is prevented.
 */
'form submit': function( el, ev ){
	ev.preventDefault();
	new Blog.Models.Post(el.formParams()).save();
},
/**
 * Listens for posts being created.	 When a post is created, displays the new post.
 * @param {String} called The open ajax event that was called.
 * @param {Event} post The new post.
 */
'post.created subscribe': function( called, post ){
	$("#post tbody").append( this.view("list", {posts:[post]}) );
	$("#post form input[type!=submit]").val(""); //clear old vals
},
 /**
 * Creates and places the edit interface.
 * @param {jQuery} el The post's edit link element.
 */
'.edit click': function( el ){
	var post = el.closest('.post').model();
	post.elements().html(this.view('edit', post));
},
 /**
 * Removes the edit interface.
 * @param {jQuery} el The post's cancel link element.
 */
'.cancel click': function( el ){
	this.show(el.closest('.post').model());
},
 /**
 * Updates the post from the edit values.
 */
'.update click': function( el ){
	var $post = el.closest('.post'); 
	$post.model().update($post.formParams());
},
 /**
 * Listens for updated posts.	 When a post is updated, 
 * update's its display.
 */
'post.updated subscribe': function( called, post ){
	this.show(post);
},
 /**
 * Shows a post's information.
 */
show: function( post ){
	post.elements().html(this.view('show',post));
},
 /**
 *	 Handle's clicking on a post's destroy link.
 */
'.destroy click': function( el ){
	if(confirm("Are you sure you want to destroy?")){
		el.closest('.post').model().destroy();
	}
 },
 /**
 *	 Listens for posts being destroyed and removes them from being displayed.
 */
"post.destroyed subscribe": function(called, post){
	post.elements().remove();	 //removes ALL elements
 }
});