import Navbar from "../components/Navbar";

export default function ProfilePage() {
  // mock data for now
  const user = {
    name: "John Doe",
    email: "john.doe@email.com",
    role: "User",
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <main className="max-w-4xl mx-auto px-6 py-10">

        <h1 className="text-3xl font-bold mb-8">
          Profile
        </h1>

        <div className="bg-white rounded-2xl shadow-lg p-8">

          {/* Avatar */}
          <div className="flex items-center gap-6 mb-8">
            <div className="w-24 h-24 bg-gray-200 rounded-full flex items-center justify-center text-2xl font-bold text-gray-600">
              JD
            </div>

            <div>
              <h2 className="text-xl font-semibold">
                {user.name}
              </h2>
              <p className="text-gray-500">
                {user.email}
              </p>
            </div>
          </div>

          {/* Info grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

            <div className="border rounded-xl p-4">
              <p className="text-sm text-gray-500">Full name</p>
              <p className="font-medium">{user.name}</p>
            </div>

            <div className="border rounded-xl p-4">
              <p className="text-sm text-gray-500">Email</p>
              <p className="font-medium">{user.email}</p>
            </div>

            <div className="border rounded-xl p-4">
              <p className="text-sm text-gray-500">Role</p>
              <p className="font-medium">{user.role}</p>
            </div>

            <div className="border rounded-xl p-4">
              <p className="text-sm text-gray-500">Account status</p>
              <p className="font-medium text-green-600">Active</p>
            </div>
          </div>

          {/* Actions */}
          <div className="mt-10 flex gap-4">
            <button className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition">
              Edit profile
            </button>

            <button className="border border-gray-300 px-6 py-2 rounded-lg hover:bg-gray-50 transition">
              Change password
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}
