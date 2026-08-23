/**
 * Recipes Controller.
 *
 */
recipesApp.controller('recipeListController', ['$scope', '$location', 'recipesService', '$log',
    function ($scope, $location, recipesService, $log) {

        var loadRecipes = function loadRecipes(){
            var pageNo = $scope.currentPage;
            recipesService.getRecipes(pageNo).$promise.then(function (recipes) {
                populateRecipesIntoScope(recipes);
            });
        };

        var populateRecipesIntoScope = function populateRecipesIntoScope(recipes){
            $scope.recipeList = recipes.recipes;
            $scope.totalItems = recipes.totalItems;
            if($scope.recipeList && $scope.recipeList.length > 0){
                for(var i=0;i<$scope.recipeList.length;i++){
                  var ingredients = "";
                  for(var j=0;j<$scope.recipeList[i].mainIngredients.length;j++){
                    ingredients = ingredients + $scope.recipeList[i].mainIngredients[j] + ",";
                  }
                  $scope.recipeList[i].ingredients = ingredients.substring(0,ingredients.lastIndexOf(",")) ;
                }
            }else{
                $scope.hasRecipes = false;
            }
        };

        $scope.getSurpriseRecipe = function getSurpriseRecipe(excludeId) {
            var targetExclude = excludeId || $scope.lastPickedRecipeId;
            recipesService.getRandomRecipe(targetExclude).$promise.then(function (recipe) {
                if (recipe && recipe.id && recipe.name) {
                    $scope.lastPickedRecipeId = recipe.id;
                    $location.path('/recipe/' + recipe.id + '/' + encodeURIComponent(recipe.name));
                }
            });
        };

        //Populate the scope.
        var defineScope = function defineScope() {
            $scope.recipeList = [];
            $scope.hasRecipes = true;
            $scope.currentPage = 1;
            $scope.totalItems = 0;
            $scope.lastPickedRecipeId = null;
            $scope.pageChanged = function(){
                $log.log('Page changed to: ' + $scope.currentPage);
                loadRecipes();
            };
            loadRecipes();
        };

        //Self executing function which acts as a constructor for the controller.
        (function init() {
            defineScope();
        })();

    }]);
