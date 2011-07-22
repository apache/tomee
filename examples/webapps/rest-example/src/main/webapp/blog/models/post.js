/**
 * @tag models, home
 * Wraps backend post services.  Enables 
 * [Blog.Models.Post.static.findAll retrieving],
 * [Blog.Models.Post.static.update updating],
 * [Blog.Models.Post.static.destroy destroying], and
 * [Blog.Models.Post.static.create creating] posts.
 */
$.Model.extend('Blog.Models.Post',
/* @Static */
{
	/**
 	 * Retrieves posts data from your backend services.
 	 * @param {Object} params params that might refine your results.
 	 * @param {Function} success a callback function that returns wrapped post objects.
 	 * @param {Function} error a callback function for an error in the ajax request.
 	 */
	findAll: function( params, success, error ){
	  self = this;
		$.ajax({
			url: '/rest-example-1.1-SNAPSHOT/api/post/list',
			type: 'get',
			dataType: 'json',
			data: params,
			success: function(posts){
                  var wrapped = self.wrapMany(posts.post);
                  success(wrapped);
              },
			error: error
		});
	},
	/**
	 * Updates a post's data.
	 * @param {String} id A unique id representing your post.
	 * @param {Object} attrs Data to update your post with.
	 * @param {Function} success a callback function that indicates a successful update.
 	 * @param {Function} error a callback that should be called with an object of errors.
     */
	update: function( id, attrs, success, error ){
		$.ajax({
			url: '/rest-example-1.1-SNAPSHOT/api/post/update/' + id,
			type: 'post',
			dataType: 'json',
			data: attrs,
			success: success,
			error: error
		});
	},
	/**
 	 * Destroys a post's data.
 	 * @param {String} id A unique id representing your post.
	 * @param {Function} success a callback function that indicates a successful destroy.
 	 * @param {Function} error a callback that should be called with an object of errors.
	 */
	destroy: function( id, success, error ){
		$.ajax({
			url: '/rest-example-1.1-SNAPSHOT/api/post/delete/' + id,
			type: 'delete',
			dataType: 'json',
			success: success,
			error: error
		});
	},
	/**
	 * Creates a post.
	 * @param {Object} attrs A post's attributes.
	 * @param {Function} success a callback function that indicates a successful create.  The data that comes back must have an ID property.
	 * @param {Function} error a callback that should be called with an object of errors.
	 */
	create: function( attrs, success, error ){
		$.ajax({
			url: '/rest-example-1.1-SNAPSHOT/api/post/create',
			type: 'put',
			dataType: 'json',
			success: success,
			error: error,
			data: attrs
		});
	}
},
/* @Prototype */
{});