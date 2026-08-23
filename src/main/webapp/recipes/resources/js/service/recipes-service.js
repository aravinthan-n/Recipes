/**
 * Recipes Service.
 *
 */
recipesApp.service('recipesService', ['recipesResource', function (recipesResource) {

    this.getRecipes = function getRecipes(pageNo){
        var param = {};
        if(pageNo){
            param.pageNo=pageNo;
        }
        return recipesResource.get(param, function(data) {
            // Empty success handler as promise is returned above.
        }, function(error) {
            // TODO: handle error
        });
    };

    this.getRandomRecipe = function getRandomRecipe(excludeId){
        var param = {};
        if (excludeId) {
            param.excludeId = excludeId;
        }
        return recipesResource.getRandom(param, function(data) {
            // Empty success handler as promise is returned above.
        }, function(error) {
            // TODO: handle error
        });
    };
}]);

recipesApp.factory("recipesResource", ['$resource', function($resource) {
	return $resource('../api/recipe', {}, {
        getRandom: { method: 'GET', url: '../api/recipe/random' }
    });
}]);