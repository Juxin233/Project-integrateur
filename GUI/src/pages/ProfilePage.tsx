import Navbar from "../components/Navbar";

export default function ProfilePage() {
  const user = {
    name: "John Doe",
    email: "john.doe@email.com",
    role: "User",
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="max-w-3xl">
          <h1 className="text-2xl font-semibold text-slate-900 mb-6">Account Settings</h1>

          <div className="bg-white rounded-lg border border-slate-200 overflow-hidden">
            {/* Header Section */}
            <div className="p-6 border-b border-slate-100 flex items-center gap-4">
              <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center text-xl font-bold text-slate-600">
                {user.name.charAt(0)}
              </div>
              <div>
                <h2 className="text-lg font-medium text-slate-900">{user.name}</h2>
                <p className="text-sm text-slate-500">{user.role}</p>
              </div>
            </div>

            {/* Details Grid */}
            <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1">Full Name</label>
                <div className="text-sm font-medium text-slate-900 border-b border-slate-100 py-2">
                  {user.name}
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1">Email Address</label>
                <div className="text-sm font-medium text-slate-900 border-b border-slate-100 py-2">
                  {user.email}
                </div>
              </div>
              
              <div>
                <label className="block text-xs font-medium text-slate-500 uppercase tracking-wider mb-1">Status</label>
                <div className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800 mt-2">
                  Active
                </div>
              </div>
            </div>

            {/* Footer Actions */}
            <div className="bg-slate-50 px-6 py-4 border-t border-slate-200 flex gap-3">
              <button className="px-4 py-2 bg-white border border-slate-300 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition shadow-sm">
                Edit Profile
              </button>
              <button className="px-4 py-2 bg-white border border-slate-300 rounded-md text-sm font-medium text-slate-700 hover:bg-slate-50 hover:text-slate-900 transition shadow-sm">
                Change Password
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}