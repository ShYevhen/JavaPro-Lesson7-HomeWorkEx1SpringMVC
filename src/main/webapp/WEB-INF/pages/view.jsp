<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>View all photos</title>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
	<form action="download_images" method="post">
		<div class="container">
			<h3>Images List</h3>
			<nav class="navbar navbar-default">
				<div class="container-fluid">
					<!-- Collect the nav links, forms, and other content for toggling -->
					<div class="collapse navbar-collapse"
						id="bs-example-navbar-collapse-1">
						<ul id="groupList" class="nav navbar-nav">
							<li><button type="submit" class="btn btn-default navbar-btn">Download
									images</button></li>
							<li><button type="button" id="delete_images"
									class="btn btn-default navbar-btn">Delete images</button></li>
							<li><button type="button" id="go_back"
									class="btn btn-default navbar-btn">Back</button></li>
						</ul>
					</div>
					<!-- /.navbar-collapse -->
				</div>
				<!-- /.container-fluid -->
			</nav>
			<table class="table table-striped">
				<thead>
					<tr>
						<th></th>
						<th>Image id</th>
						<th>Image</th>
					</tr>
				</thead>
				<c:forEach items="${photos}" var="photo_id">
					<tr>
						<td><input type="checkbox" name="toDo[]" value="${photo_id}"
							id="checkbox_${photo_id}" onclick="robotForm(this.form)"></td>
						<td><a href="/Lesson7HomeWorkEx1SpringMVC/photo/${photo_id}">${photo_id}</a></td>
						<td><img src="/Lesson7HomeWorkEx1SpringMVC/photo/${photo_id}"
							height="100" alt="${photo_id}" />
				</c:forEach>
			</table>
			<nav aria-label="Page navigation">
				<ul class="pagination">
					<c:forEach var="i" begin="1" end="${pages}">
						<li><a href="?page=<c:out value="${i - 1}"/>"><c:out
									value="${i}" /></a></li>
					</c:forEach>
				</ul>
			</nav>

		</div>
		<script type="text/javascript">
			$('#delete_images').click(function() {
				var data = {
					'toDelete[]' : []
				};
				$(":checked").each(function() {
					data['toDelete[]'].push($(this).val());
				});
				$.post("./delete_images", data, function(data, status) {
					window.location.reload();
				});
			});
			$('#go_back').click(function() {
				window.location.href = './';
			});
		</script>
	</form>
</body>
</html>